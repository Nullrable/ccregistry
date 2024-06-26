package org.lsd.ccregistery.health;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.lsd.ccregistery.model.InstanceMeta;
import org.lsd.ccregistery.service.CcRegistryService;
import org.lsd.ccregistery.service.RegistryService;

/**
 * @author nhsoft.lsd
 */
@Slf4j
public class CcHealthChecker implements HealthChecker{

    private long timeout = 60_000;
    private long interval = 1000;

    private RegistryService registryService;

    private ScheduledExecutorService executor;

    public CcHealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public void start() {
        if (executor != null && !executor.isShutdown()) {
            return;
        }
        executor = new ScheduledThreadPoolExecutor(1);

        executor.scheduleWithFixedDelay(
                () -> {
                    long now = System.currentTimeMillis();

                    CcRegistryService.TIMESTAMPS.forEach((serviceAndInstance, timestamp) -> {

                        int index = serviceAndInstance.indexOf("@");
                        String service = serviceAndInstance.substring(0, index);
                        String url = serviceAndInstance.substring(index + 1);

                        log.info(" ===>>> health check service and instance {} ", serviceAndInstance);

                        if (now - timestamp > timeout) {
                            registryService.unregister(service, InstanceMeta.from(url));
                            log.info(" ===>>> health check service and instance {} is timeout", serviceAndInstance);
                            CcRegistryService.TIMESTAMPS.remove(serviceAndInstance);
                        }
                    });
                },
                0, interval, TimeUnit.MILLISECONDS);

        log.info(" ===>>> health checker started...");
    }

    @Override
    public void stop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        log.info(" ===>>> health checker stopped...");
    }
}
