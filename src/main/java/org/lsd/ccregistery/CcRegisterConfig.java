package org.lsd.ccregistery;

import org.lsd.ccregistery.cluster.Cluster;
import org.lsd.ccregistery.health.CcHealthChecker;
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

    @Bean(initMethod = "start", destroyMethod = "stop")
    public CcHealthChecker healthChecker(RegistryService registryService) {
        return new CcHealthChecker(registryService);
    }

    @Bean(initMethod = "init")
    public Cluster cluster(CcRegistryProperties ccRegistryProperties) {
        return new Cluster(ccRegistryProperties, port);
    }
}
