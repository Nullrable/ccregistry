package org.lsd.ccregistery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class CcRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcRegistryApplication.class, args);
    }
}
