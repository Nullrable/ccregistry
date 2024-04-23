package org.lsd.ccregistery.cluster;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.lsd.ccregistery.CcRegistryProperties;
import org.lsd.ccregistery.health.HealthChecker;
import org.lsd.ccregistery.model.InstanceMeta;
import org.lsd.ccregistery.model.Snapshot;
import org.lsd.ccregistery.service.CcRegistryService;
import org.lsd.ccregistery.service.RegistryService;
import org.lsd.ccregistery.util.HttpInvoker;
import org.lsd.ccregistery.util.InetUtils;
import org.springframework.util.LinkedMultiValueMap;

/**
 * @author nhsoft.lsd
 */
@Slf4j
public class Cluster {

    private RegistryService registryService;

    private HealthChecker healthChecker;

    private int port;

    private CcRegistryProperties ccRegistryProperties;

    private Server MYSELF;

    public List<Server> servers;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public Cluster(final CcRegistryProperties ccRegistryProperties,
                   final RegistryService registryService,
                   final HealthChecker healthChecker,
                   final int port) {
        this.ccRegistryProperties = ccRegistryProperties;
        this.registryService = registryService;
        this.healthChecker = healthChecker;
        this.port = port;
    }

    public void init() {
        String ip;
        try {
            ip = InetUtils.getHostIp();
        } catch (UnknownHostException e) {
            ip = "127.0.0.1";
        }

        MYSELF = new Server("http://" + ip + ":" + port, true, false, -1);

        List<String> serverList = ccRegistryProperties.getServerList();

        List<Server> servers = new ArrayList<>();
        for (String url : serverList) {
            Server server = new Server();
            if (url.contains("localhost")) {
                url = url.replace("localhost", ip);
            } else if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", ip);
            }
            if (url.equals(MYSELF.getUrl())) {
                servers.add(MYSELF);
            } else {
                server.setUrl(url);
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(-1L);
                servers.add(server);
            }
        }

        this.servers = servers;

        executor.scheduleWithFixedDelay(() -> {
            try {
                // 需要先更新服务信息，需要看看当前集群中是否存在主节点
                updateServer();

                // 再尝试选举主节点
                electLeader();

                //从 同步 主的数据信息
                syncFromLeader();

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }, 1, 5, TimeUnit.SECONDS);

        log.info(" ===>>> cluster started");
    }

    private void syncFromLeader() {
        if (MYSELF.isLeader()) {
            return;
        }
        Server leader = servers.stream().filter(Server::isLeader).findAny().orElse(null);
        if (leader == null) {
            return;
        }
        try {
            Snapshot snapshot = HttpInvoker.get(leader.getUrl() + "/snapshot", Snapshot.class);

            LinkedMultiValueMap<String, InstanceMeta> map = new LinkedMultiValueMap<>();
            snapshot.getRegistry().forEach((key, values) -> {
                values.forEach(v -> {
                    JSONObject jsonObject = (JSONObject)v;
                    map.add(key, jsonObject.toJavaObject(InstanceMeta.class));
                });

            });
            snapshot.setRegistry(map);
            //TODO LinkedMultiValueMap value 是 JSONObject，这列需要改造
            registryService.restore(snapshot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void electLeader() {
        List<Server> leaders = servers.stream().filter(Server::isStatus).filter(Server::isLeader).toList();
        if (leaders.isEmpty()) {
            log.info(" ===>>> no leader found");
            elect();
        } else if (leaders.size() > 1) {
            log.info(" ===>>> multiple leader found");
            elect();
        } else {
            log.info(" ===>>> single leader found");
        }

        if (MYSELF.isLeader()) {
            healthChecker.start();
        } else {
            healthChecker.stop();
        }
    }

    private void elect() {
        //定时选举Leader
        // 1, 各自选择，通过一定策略，选举出来的leader是同一个
        // 2, 分布式锁，谁获得锁，谁就是 leader
        // 3，通过raft、paxos 一致性算法
        Server candidate = null;
        for (Server server : servers) {
            server.setLeader(false);
            if (server.isStatus()) {
                if (candidate == null) {
                    candidate = server;
                } else {
                    if (candidate.hashCode() > server.hashCode()) {
                        candidate = server;
                    }
                }
            }
        }
        if (candidate != null) {
            candidate.setLeader(true);
            log.info(" ===>>> elect for leader: {}", candidate);
        } else {
            log.info(" ===>>> elect failed for no leaders: {}", servers);
        }
    }

    private void updateServer() {
        //定时调用Server服务，更新 status 状态.
        //这里改为并行调用，几个服务就可以同时进行，不然如果串行，servers 多的情况下，最后一个调用会是前几个调用延迟的累加
        servers.parallelStream().forEach(server -> {
            try {
                if (server.equals(MYSELF)) {
                    return;
                }
                Server serverInfo = HttpInvoker.get(server.getUrl() + "/info", Server.class);
                server.setStatus(true);
                server.setLeader(serverInfo.isLeader());
                server.setVersion(serverInfo.getVersion());
                log.info(" ===>>> update server: {}", server);
            } catch (Exception e) {
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    public Server self() {
        MYSELF.setVersion(CcRegistryService.VERSION.get());
        return MYSELF;
    }

    public void destroy(){
        healthChecker.stop();
        executor.shutdown();
    }
}
