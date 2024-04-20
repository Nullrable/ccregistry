package org.lsd.ccregistery.model;

import com.alibaba.fastjson.JSON;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author nhsoft.lsd
 */
@Data
@EqualsAndHashCode(of = {"scheme", "host", "port", "context"})
public class InstanceMeta {

    private String scheme;

    private String host;

    private int port;

    private String context;

    private boolean status = false; // online or offline

    private Map<String, String> parameters = new HashMap<>();  // idc  A B C

    public InstanceMeta(String scheme, String host, Integer port, String context) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", scheme, host, port, context);
    }

    public static InstanceMeta from(String url) {
        URI uri = URI.create(url);
        return new InstanceMeta(uri.getScheme(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath().substring(1));
    }

    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "ccrpc");
    }

    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }


}
