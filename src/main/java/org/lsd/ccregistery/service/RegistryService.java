package org.lsd.ccregistery.service;

import java.util.List;
import java.util.Map;
import org.lsd.ccregistery.model.InstanceMeta;
import org.lsd.ccregistery.model.Snapshot;

/**
 * @author nhsoft.lsd
 */
public interface RegistryService {

    //基础功能

    /**
     * register service.
     *
     * @param service 服务名称，例如 com.xxx.userService
     * @param instance 运行的节点
     */
    void register(String service, InstanceMeta instance);

    /**
     * unregister register.
     *
     * @param service 服务名称，例如 com.xxx.userService
     * @param instance 运行的节点
     */
    void unregister(String service, InstanceMeta instance);

    /**
     * fetch instance.
     *
     * @param service 服务名称，例如 com.xxx.userService
     * @return 返回服务节点
     */
    List<InstanceMeta> fetchAll(String service);

    //高级功能
    Long renew(InstanceMeta instance, String... service);

    Long version(String service);

    Map<String, Long> versions(String... versions);

    void subscribe(String service);

    void unsubscribe(String service);

    Snapshot snapshot();

    void restore(Snapshot snapshot);
}
