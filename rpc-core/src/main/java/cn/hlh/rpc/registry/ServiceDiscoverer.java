package cn.hlh.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 */
public interface ServiceDiscoverer {
    /**
     * 服务发现方法
     * @param serviceName 服务名称
     * @return 对应服务器的IP和端口地址
     */
    InetSocketAddress lookupService(String serviceName);
}
