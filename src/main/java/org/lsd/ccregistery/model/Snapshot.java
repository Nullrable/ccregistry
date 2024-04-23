package org.lsd.ccregistery.model;

import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.LinkedMultiValueMap;

/**
 * @author nhsoft.lsd
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class Snapshot {

    private LinkedMultiValueMap<String, ?> registry;

    private Long version;

    private HashMap<String, Long> versions;

    private  HashMap<String, Long> timestamps;
}
