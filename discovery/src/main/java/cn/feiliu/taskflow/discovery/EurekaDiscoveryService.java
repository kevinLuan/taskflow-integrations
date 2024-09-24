package cn.feiliu.taskflow.discovery;

import cn.feiliu.taskflow.client.spi.DiscoveryService;
import com.netflix.discovery.EurekaClient;

/**
 * @author SHOUSHEN.LUAN
 * @since 2024-09-24
 */
public class EurekaDiscoveryService implements DiscoveryService {
    private final EurekaClient client;

    public EurekaDiscoveryService(EurekaClient client) {
        this.client = client;
    }

    @Override
    public ApplicationStatus getStatus() {
        return ApplicationStatus.valueOf(client.getInstanceRemoteStatus().name());
    }
}
