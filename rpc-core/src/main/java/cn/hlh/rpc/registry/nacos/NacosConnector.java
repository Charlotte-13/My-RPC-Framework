package cn.hlh.rpc.registry.nacos;

import cn.hlh.rpc.enumeration.EmRpcError;
import cn.hlh.rpc.exception.RpcException;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 管理Nacos连接的通用方法
 */
public class NacosConnector {
    private static final Logger logger = LoggerFactory.getLogger(NacosConnector.class);
    private static final String SERVER_ADDR = "127.0.0.1:8848";
    private static final NamingService namingService = getNamingService();
    private static final Set<String> serviceNames = new HashSet<>();
    private static InetSocketAddress address;

    public static NamingService getNamingService(){
        try {
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            logger.error("连接到Nacos时有错误发生: ", e);
            throw new RpcException(EmRpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    public static void registerService(String serviceName, InetSocketAddress inetSocketAddress) throws NacosException {
        namingService.registerInstance(serviceName,inetSocketAddress.getAddress().getHostAddress(),inetSocketAddress.getPort());
        serviceNames.add(serviceName);
        address = inetSocketAddress;
    }

    public static List<Instance> getAllServiceInstance(String serviceName) throws NacosException {
        List<Instance> instances = namingService.getAllInstances(serviceName);
        if(instances.size()==0){
            logger.error("找不到对应的服务: " + serviceName);
            throw new RpcException(EmRpcError.SERVICE_NOT_FOUND);
        }
        return instances;
    }

    /**
     * 清除该服务器注册在服务中心的服务
     */
    public static void clearRegistry(){
        if(!serviceNames.isEmpty()&&address!=null){
            Iterator<String> iterator = serviceNames.iterator();
            if(iterator.hasNext()){
                String serviceName = iterator.next();
                try {
                    namingService.deregisterInstance(serviceName,address.getHostName(),address.getPort());
                } catch (NacosException e) {
                    logger.error("注销服务 {} 失败", serviceName, e);
                }
            }
        }
    }
}
