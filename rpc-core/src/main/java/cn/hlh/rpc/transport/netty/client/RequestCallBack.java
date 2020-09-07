package cn.hlh.rpc.transport.netty.client;

import cn.hlh.rpc.entity.RpcRequest;
import cn.hlh.rpc.entity.RpcResponse;
import cn.hlh.rpc.enumeration.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * 该对象用于实现服务端对异步等待远程调用的返回结果
 */
public class RequestCallBack {
    private static final Logger logger = LoggerFactory.getLogger(RequestCallBack.class);
    //该类的静态变量，用于存放所有尚未从服务端返回调用结果的异步回调对象；key为请求id
    private static final ConcurrentHashMap<String, RequestCallBack> callBackMap = new ConcurrentHashMap<>();
    //封装在该类内部的调用请求对象
    private final RpcRequest request;
    //对调用请求对象响应的结果，用CompletableFuture实现异步调用
    private final CompletableFuture<RpcResponse> responseFuture;

    public RequestCallBack(RpcRequest request){
        this.request = request;
        this.responseFuture = new CompletableFuture<>();
        //每一个创建的异步回调对象都会被直接保存
        callBackMap.put(request.getRequestId(),this);
    }

    //用于外部调用删除无效的异步回调对象
    public static void remove(String requestId){
        callBackMap.remove(requestId);
    }

    //用于外部根据请求id获取异步回调对象
    public static RequestCallBack getInstance(String requestId){
        return callBackMap.get(requestId);
    }

    //将获取的响应结果传入异步回调对象
    public void complete(RpcResponse response){
        responseFuture.complete(response);
        //将已返回的异步调用对象删除；
        callBackMap.remove(response.getRequestId());
    }

    //该方法用于异步获取调用请求的返回值
    public RpcResponse start(){
        RpcResponse response = null;
        try {
            response = responseFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("获取异步回调结果时出错：",e);
            response = RpcResponse.fail(e, ResponseCode.FAIL);
        }
        return response;
    }
}
