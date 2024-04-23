package org.lsd.ccregistery;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import org.lsd.ccregistery.cluster.Cluster;
import org.lsd.ccregistery.cluster.Server;
import org.lsd.ccregistery.exception.CcRegistryException;
import org.lsd.ccregistery.model.InstanceMeta;
import org.lsd.ccregistery.model.Snapshot;
import org.lsd.ccregistery.service.CcRegistryService;
import org.lsd.ccregistery.service.RegistryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * register controller
 *
 * @author nhsoft.lsd
 */
@RestController
public class CcRegistryController {

    @Resource
    private RegistryService registryService;

    @Resource
    private Cluster cluster;


    @PostMapping("/register")
    public void register(@RequestParam("service") final String service,  @RequestBody final InstanceMeta instance) {

        checkLeader();

        registryService.register(service, instance);
    }

    @PostMapping("/unregister")
    public void unregister(@RequestParam("service") final String service,  @RequestBody final InstanceMeta instance) {

        checkLeader();

        registryService.unregister(service, instance);
    }

    @GetMapping("/fetchAll")
    public List<InstanceMeta> fetchAll(@RequestParam("service") final String service) {
       return registryService.fetchAll(service);
    }

    @GetMapping("/heartbeat")
    public Long heartbeat(@RequestParam("services") final String services,  @RequestBody final InstanceMeta instance) {

        checkLeader();

        return registryService.renew(instance, services.split(","));
    }

    @GetMapping("/timestamps")
    public Map<String, Long> timestamps() {
        return CcRegistryService.TIMESTAMPS;
    }

    @GetMapping("/version")
    public Long version(@RequestParam("service") final String service) {
        return registryService.version(service);
    }

    @GetMapping("/versions")
    public Map<String, Long> versions(@RequestParam("services") final String services) {
        return registryService.versions(services.split(","));
    }

    @GetMapping("/info")
    public Server info() {
        return cluster.self();
    }

    @GetMapping("/snapshot")
    public Snapshot snapshot() {
        return registryService.snapshot();
    }

    @GetMapping("/cluster")
    public List<Server> cluster() {
        return cluster.servers;
    }

    private void checkLeader() {
        if (!cluster.self().isLeader()) {
            throw new CcRegistryException("current instance is not leader");
        }
    }
}
