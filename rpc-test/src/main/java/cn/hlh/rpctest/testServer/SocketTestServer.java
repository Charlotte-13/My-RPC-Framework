package cn.hlh.rpctest.testServer;

import cn.hlh.rpc.provider.DefaultServiceProvider;
import cn.hlh.rpc.provider.ServiceProvider;
import cn.hlh.rpc.transport.socket.server.SocketServer;
import cn.hlh.rpctest.testApi.HelloService;

public class SocketTestServer {
    public static void main(String[] args) {
//        HelloService helloService = new HelloServiceImpl();
//        ServiceProvider registry = new DefaultServiceProvider();
//        registry.register(helloService);
        SocketServer server = new SocketServer();
        server.start();
    }
}
