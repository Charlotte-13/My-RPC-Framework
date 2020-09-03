package cn.hlh.rpc.transport;

import cn.hlh.rpc.annotation.Service;
import cn.hlh.rpc.annotation.ServiceScan;
import cn.hlh.rpc.enumeration.EmRpcError;
import cn.hlh.rpc.exception.RpcException;
import cn.hlh.rpc.factory.SingletonFactory;
import cn.hlh.rpc.provider.DefaultServiceProvider;
import cn.hlh.rpc.provider.ServiceProvider;
import cn.hlh.rpc.registry.nacos.NacosServiceRegistrar;
import cn.hlh.rpc.registry.ServiceRegistrar;
import cn.hlh.rpc.util.ReflectUtil;
import cn.hlh.rpc.util.RpcServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Set;

/**
 * 用抽象类实现服务端的通用方法，减少代码量
 */
public abstract class AbstractRpcServer implements RpcServer {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected int port;
    //本地服务注册表
    protected ServiceProvider serviceProvider = SingletonFactory.getInstance(DefaultServiceProvider.class);
    //远程服务注册中心
    protected ServiceRegistrar serviceRegistrar = SingletonFactory.getInstance(NacosServiceRegistrar.class);

    @Override
    public <T> void publishService(T service) {
        serviceProvider.register(service);
        serviceRegistrar.register(service,new InetSocketAddress(RpcServerUtil.getCurrentIp(),port));
    }

    @Override
    public void scanServicesByAnnotation(int port) {
        this.port = port;
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass = null;
        try {
            startClass = Class.forName(mainClassName);
        } catch (ClassNotFoundException e) {
            logger.error("出现未知错误：",e);
            throw new RpcException(EmRpcError.UNKNOWN_ERROR);
        }
        if(!startClass.isAnnotationPresent(ServiceScan.class)){
            logger.error("启动类缺少 @ServiceScan 注解");
            throw new RpcException(EmRpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
        }
        String packageName = startClass.getAnnotation(ServiceScan.class).value();
        if("".equals(packageName)){
            packageName = mainClassName.substring(0,mainClassName.lastIndexOf("."));
        }
        Set<Class<?>> classes = null;
        try {
            classes = ReflectUtil.getClasses(packageName);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("在扫描用户定义视图时，从指定包中获取类出错：",e);
            throw new RpcException(EmRpcError.FAILED_TO_GET_CLASS_FROM_SERVICE_PACKAGE);
        }
        for(Class<?> clazz : classes){
            if(clazz.isAnnotationPresent(Service.class)){
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object service = null;
                try {
                    service = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("创建 " + clazz + " 时有错误{ }发生",e);
                    continue;
                }
                publishService(service);
            }
        }
    }
}
