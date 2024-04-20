package org.lsd.ccregistery.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author nhsoft.lsd
 */
@Data
@EqualsAndHashCode(of = "url")
@AllArgsConstructor
@NoArgsConstructor
public class Server {

    private String url;

    private boolean status;

    private boolean leader;

    private long version;
}
