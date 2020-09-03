package cn.hlh.rpctest.testClient;

import cn.hlh.rpc.enumeration.SerializerCode;
import cn.hlh.rpc.transport.RpcClientProxy;
import cn.hlh.rpc.transport.netty.client.NettyClient;
import cn.hlh.rpctest.testApi.HelloObject;
import cn.hlh.rpctest.testApi.HelloService;

public class NettyTestClient {
    public static void main(String[] args) throws InterruptedException {
        HelloObject helloObject = new HelloObject();
        helloObject.setId(12);
        helloObject.setMessage("This is a netty message");
        NettyClient client = new NettyClient(SerializerCode.Kryo);
        RpcClientProxy proxy = new RpcClientProxy(client);
        HelloService helloService = proxy.getProxy(HelloService.class);
        String hello = helloService.hello(helloObject);
        System.out.println(hello);
        Thread.sleep(30000);
        hello = helloService.hello(helloObject);
        System.out.println(hello);
    }
}
