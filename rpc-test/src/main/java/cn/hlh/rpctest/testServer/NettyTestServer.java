package cn.hlh.rpctest.testServer;


import cn.hlh.rpc.annotation.ServiceScan;
import cn.hlh.rpc.enumeration.SerializerCode;
import cn.hlh.rpc.provider.DefaultServiceProvider;
import cn.hlh.rpc.provider.ServiceProvider;
import cn.hlh.rpc.transport.netty.server.NettyServer;
import cn.hlh.rpctest.testApi.HelloService;

@ServiceScan
public class NettyTestServer {
    public static void main(String[] args) {
//        HelloService helloService = new HelloServiceImpl();
//        ServiceProvider registry = new DefaultServiceProvider();
//        registry.register(helloService);
        NettyServer nettyServer = new NettyServer(9000, SerializerCode.Kryo);
        nettyServer.start();
    }
}
