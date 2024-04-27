package org.lsd.ccregistery;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.lsd.ccregistery.cluster.Cluster;
import org.lsd.ccregistery.cluster.Server;
import org.lsd.ccregistery.exception.CcRegistryException;
import org.lsd.ccregistery.model.InstanceMeta;
import org.lsd.ccregistery.model.Snapshot;
import org.lsd.ccregistery.service.CcRegistryService;
import org.lsd.ccregistery.service.RegistryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * register controller
 *
 * @author nhsoft.lsd
 */
@Slf4j
@RestController
public class CcRegistryController {

    @Resource
    private RegistryService registryService;

    @Resource
    private Cluster cluster;

    @PostMapping("/register")
    public void register(@RequestParam("service") final String service,  @RequestBody final InstanceMeta instance) {

        checkLeader();

        registryService.register(service, instance);
    }

    @PostMapping("/unregister")
    public void unregister(@RequestParam("service") final String service,  @RequestBody final InstanceMeta instance) {

        checkLeader();

        registryService.unregister(service, instance);
    }

    @GetMapping("/fetchAll")
    public List<InstanceMeta> fetchAll(@RequestParam("service") final String service) {
        return registryService.fetchAll(service);
    }

    @PostMapping("/heartbeat")
    public Long heartbeat(@RequestParam("services") final String services,  @RequestBody final InstanceMeta instance) {

        checkLeader();

        return registryService.renew(instance, services.split(","));
    }

    @GetMapping("/timestamps")
    public Map<String, Long> timestamps() {
        return CcRegistryService.TIMESTAMPS;
    }

    @GetMapping("/version")
    public Long version(@RequestParam("service") final String service) {
        return registryService.version(service);
    }

    @GetMapping("/versions")
    public Map<String, Long> versions(@RequestParam("services") final String services) {
        return registryService.versions(services.split(","));
    }

    @GetMapping("/info")
    public Server info() {
        return cluster.self();
    }

    @GetMapping("/snapshot")
    public Snapshot snapshot() {
        return registryService.snapshot();
    }

    @GetMapping("/cluster")
    public List<Server> cluster() {
        return cluster.servers;
    }

    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
    private final Map<String, Long> VERSIONS = new HashMap<>();
    private final Map<String, Future> scheduledFutures = new HashMap<>();
    private final long LONG_POLLING_TIMEOUT = 60_000L;

    @GetMapping("/subscribe")
    public DeferredResult<String> subscribe(@RequestParam("service") final String service) {
        DeferredResult<String> deferredResult = new DeferredResult<>(LONG_POLLING_TIMEOUT);
        // 返回一个 DeferredResult 对象
        Future future = executorService.submit(new LongPullingTask(service, deferredResult));
        scheduledFutures.put(service, future);
        return deferredResult;
    }

    @GetMapping("/unsubscribe")
    @ResponseStatus(HttpStatus.OK)
    public void unsubscribe(@RequestParam("service") final String service) {
        Future future = scheduledFutures.get(service);
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
    }

    class LongPullingTask implements Runnable {
        private String service;
        private DeferredResult<String> deferredResult;

        public LongPullingTask(final String service, final DeferredResult<String> deferredResult) {
            this.service = service;
            this.deferredResult = deferredResult;
        }

        @Override
        public void run() {
            try {
                // 模拟长时间运行的任务
                Long currentVersion = VERSIONS.getOrDefault(service, -1L);
                Long latestVersion = -1L;

                while (currentVersion >= latestVersion) {
                    latestVersion = Optional.ofNullable(registryService.version(service)).orElse(-1L);;
                    VERSIONS.put(service, latestVersion);
                    Thread.sleep(1000);
                }
                List<InstanceMeta> instanceMetas = registryService.fetchAll(service);
                deferredResult.setResult(JSON.toJSONString(instanceMetas)); // 设置结果
                log.info(" ====>>>> instance changed:{}", instanceMetas);

            } catch (Exception e) {
                deferredResult.setErrorResult(e.getMessage()); // 设置错误结果
            }
        }
    }

    private void checkLeader() {
        if (!cluster.MYSELF.isLeader()) {
            throw new CcRegistryException("current instance is not leader");
        }
    }
}
