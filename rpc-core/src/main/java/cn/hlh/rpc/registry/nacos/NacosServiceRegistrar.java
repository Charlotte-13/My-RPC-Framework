package cn.hlh.rpc.registry.nacos;

import cn.hlh.rpc.enumeration.EmRpcError;
import cn.hlh.rpc.exception.RpcException;
import cn.hlh.rpc.registry.ServiceRegistrar;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


public class NacosServiceRegistrar implements ServiceRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistrar.class);

    @Override
    public <T> void register(T service, InetSocketAddress inetSocketAddress) {
        try {
            Class<?>[] interfaces = service.getClass().getInterfaces();
            for(Class<?> i : interfaces){
                NacosConnector.registerService(i.getCanonicalName(),inetSocketAddress);
            }
        } catch (NacosException e) {
            logger.error("注册服务时有错误发生:", e);
            throw new RpcException(EmRpcError.REGISTER_SERVICE_FAILED);
        }
    }
}
