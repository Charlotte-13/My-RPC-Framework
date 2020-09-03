package cn.hlh.rpc.registry.nacos;

import cn.hlh.rpc.enumeration.EmRpcError;
import cn.hlh.rpc.exception.RpcException;
import cn.hlh.rpc.loadbalancer.LoadBalancer;
import cn.hlh.rpc.loadbalancer.RandomLoadBalancer;
import cn.hlh.rpc.registry.ServiceDiscoverer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class NacosServiceDiscoverer implements ServiceDiscoverer {
    private static final Logger logger = LoggerFactory.getLogger(NacosServiceDiscoverer.class);
    private final LoadBalancer loadBalancer;
    public NacosServiceDiscoverer(){
        loadBalancer = new RandomLoadBalancer();
    }
    public NacosServiceDiscoverer(LoadBalancer loadBalancer){
        this.loadBalancer = loadBalancer;
    }
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            List<Instance> instances = NacosConnector.getAllServiceInstance(serviceName);
            Instance instance = loadBalancer.select(instances);
            return new InetSocketAddress(instance.getIp(),instance.getPort());
        } catch (NacosException e) {
            logger.error("获取服务时有错误发生:", e);
            throw new RpcException(EmRpcError.SERVICE_NOT_FOUND);
        }
    }
}
