package cn.hlh.rpc.provider;

import cn.hlh.rpc.enumeration.EmRpcError;
import cn.hlh.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认服务注册表
 */
public class DefaultServiceProvider implements ServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceProvider.class);
    //注册的接口（String）和对应服务实现类（Object）表，使用static保证全局唯一性
    private static final Map<String,Object> serviceMap = new ConcurrentHashMap<>();
    //所有已注册的服务实现类，使用static保证全局唯一性
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    private DefaultServiceProvider(){}
    /**
     * 向服务注册表中注册服务
     * @param service
     * @param <T>
     */
    @Override
    public synchronized <T> void register(T service) {
        String serviceImplName = service.getClass().getCanonicalName();
        if(registeredService.contains(serviceImplName)){
            return;
        }
        registeredService.add(serviceImplName);
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if(interfaces.length==0){
            throw new RpcException(EmRpcError.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }
        for(Class<?> i : interfaces){
            serviceMap.put(i.getCanonicalName(),service);
        }
        logger.info("向接口：{} 注册服务：{}", interfaces,service);
    }

    /**
     * 通过接口名获取指定已注册服务
     * @param interfaceName
     * @return
     */
    @Override
    public Object getService(String interfaceName) {
        Object service = serviceMap.get(interfaceName);
        if(service==null){
            throw new RpcException(EmRpcError.SERVICE_NOT_FOUND);
        }
        return service;
    }
}
