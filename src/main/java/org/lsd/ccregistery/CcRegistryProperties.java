package org.lsd.ccregistery;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nhsoft.lsd
 */
@Data
@ConfigurationProperties(prefix = "ccregistry")
public class CcRegistryProperties {

    private List<String> serverList;

}
