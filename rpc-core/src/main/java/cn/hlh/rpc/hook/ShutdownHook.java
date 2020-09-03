package cn.hlh.rpc.hook;

import cn.hlh.rpc.registry.nacos.NacosConnector;

public class ShutdownHook {
    private ShutdownHook(){}

    /**
     * 钩子方法，用于在服务器（JVM）关闭前，将该服务器在注册中心注册的服务注销
     */
    public void addClearAllHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                NacosConnector.clearRegistry();
            }
        }));
    }
}
