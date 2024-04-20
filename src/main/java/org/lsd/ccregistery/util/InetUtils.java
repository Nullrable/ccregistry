package org.lsd.ccregistery.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author nhsoft.lsd
 */
public class InetUtils {

    public static String getHostIp() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        return inetAddress.getHostAddress();
    }
}
