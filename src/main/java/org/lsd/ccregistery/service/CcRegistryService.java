package org.lsd.ccregistery.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.lsd.ccregistery.model.InstanceMeta;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author nhsoft.lsd
 */
@Slf4j
public class CcRegistryService implements RegistryService {

    private static final MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();

    private static final AtomicLong VERSION = new AtomicLong(0);

    //版本号：用于消费者发现本地服务版本 和 注册中心版本 差异，如果消费者本地版本低，说明服务在注册中心有更新，需要消费者需要拉取服务节点
    private final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();

    //时间戳：用于健康检查，通过比较时间戳来识别超时的服务实例，从而将它们从服务列表中移除。
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();


    @Override
    public void register(final String service, final InstanceMeta instance) {

        List<InstanceMeta> metas = REGISTRY.get(service);

        if (metas == null || metas.isEmpty() || !metas.contains(instance)) {
            instance.setStatus(true);
            REGISTRY.add(service, instance);

            log.info(" ===>>> register service: {} instance: {}", service, instance.toUrl());
        } else {
            instance.setStatus(true);

            log.info(" ===>>> service: {} instance: {} is already registered", service, instance.toUrl());
        }
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
    }

    @Override
    public void unregister(final String service, final InstanceMeta instance) {

        List<InstanceMeta> metas = REGISTRY.get(service);
        if (metas == null || metas.isEmpty()) {
            return;
        }

        metas.remove(instance);

        VERSIONS.put(service, VERSION.incrementAndGet());

        log.info(" ===>>> unregister service: {} instance: {}", service, instance.toUrl());
    }

    @Override
    public List<InstanceMeta> fetchAll(final String service) {
        return REGISTRY.get(service);
    }

    @Override
    public long renew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + instance.toUrl(), now);
        }
        return now;
    }

    @Override
    public long version(final String service) {
        return VERSIONS.get(service);
    }

    @Override
    public Map<String, Long> versions(final String... services) {

       return Arrays.stream(services)
                .collect(Collectors.toMap(x->x, VERSIONS::get, (a, b)->b));

    }

    @Override
    public void subscribe(final String service) {

    }

    @Override
    public void unsubscribe(final String service) {

    }
}
