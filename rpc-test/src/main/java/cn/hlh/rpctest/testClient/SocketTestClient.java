package cn.hlh.rpctest.testClient;

import cn.hlh.rpc.transport.RpcClientProxy;
import cn.hlh.rpc.transport.socket.client.SocketClient;
import cn.hlh.rpctest.testApi.HelloObject;
import cn.hlh.rpctest.testApi.HelloService;

public class SocketTestClient {
    public static void main(String[] args) {
        HelloObject helloObject = new HelloObject();
        helloObject.setId(10);
        helloObject.setMessage("this is a socket message");
        SocketClient client = new SocketClient("120.55.192.85", 9000);
        RpcClientProxy proxy = new RpcClientProxy(client);
        HelloService service = proxy.getProxy(HelloService.class);
        String hello = service.hello(helloObject);
        System.out.println(hello);
    }
}
