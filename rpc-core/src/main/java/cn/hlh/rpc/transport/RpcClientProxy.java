package cn.hlh.rpc.transport;

import cn.hlh.rpc.entity.RpcRequest;
import cn.hlh.rpc.entity.RpcResponse;
import cn.hlh.rpc.enumeration.EmRpcError;
import cn.hlh.rpc.enumeration.ResponseCode;
import cn.hlh.rpc.exception.RpcException;
import cn.hlh.rpc.transport.netty.client.RequestCallBack;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

public class RpcClientProxy implements InvocationHandler {
    private RpcClient rpcClient;
    public RpcClientProxy(RpcClient rpcClient){
        this.rpcClient = rpcClient;
    }
    public <T> T getProxy(Class<T> clazz){
        //第二个参数表示将clazz赋给Class数组下标为0的位置
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this);
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParamType(method.getParameterTypes());
        request.setHeartBeat(false);
        request.setRequestId(UUID.randomUUID().toString());
        RequestCallBack callBack = (RequestCallBack) rpcClient.sendRequest(request);
        RpcResponse response = callBack.start();
        if(response.getResponseCode()==ResponseCode.SUCCESS){
            return response.getData();
        }else {
            throw new RpcException(EmRpcError.UNKNOWN_ERROR);
        }
    }
}
