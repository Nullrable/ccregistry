package org.lsd.ccregistery;

import org.lsd.ccregistery.health.CcHealthChecker;
import org.lsd.ccregistery.service.CcRegistryService;
import org.lsd.ccregistery.service.RegistryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author nhsoft.lsd
 */
@Configuration
public class CcRegisterConfig {

    @Bean
    public RegistryService registryService() {
        return new CcRegistryService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public CcHealthChecker healthChecker(RegistryService registryService) {
        return new CcHealthChecker(registryService);
    }
}
