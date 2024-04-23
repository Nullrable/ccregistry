package org.lsd.ccregistery;

import org.lsd.ccregistery.cluster.Cluster;
import org.lsd.ccregistery.health.CcHealthChecker;
import org.lsd.ccregistery.health.HealthChecker;
import org.lsd.ccregistery.service.CcRegistryService;
import org.lsd.ccregistery.service.RegistryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author nhsoft.lsd
 */
@Configuration
@Import({CcRegistryProperties.class})
public class CcRegisterConfig {

    @Value("${server.port}")
    private int port;

    @Bean
    public RegistryService registryService() {
        return new CcRegistryService();
    }

    @Bean
    public CcHealthChecker healthChecker(RegistryService registryService) {
        return new CcHealthChecker(registryService);
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public Cluster cluster(final CcRegistryProperties ccRegistryProperties,
                           final RegistryService registryService,
                           final HealthChecker healthChecker) {
        return new Cluster(ccRegistryProperties, registryService, healthChecker, port);
    }
}
