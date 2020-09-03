package cn.hlh.rpc.registry;

import cn.hlh.rpc.loadbalancer.LoadBalancer;

import java.net.InetSocketAddress;

public interface ServiceRegistrar {
    <T> void register(T service, InetSocketAddress inetSocketAddress);
}
