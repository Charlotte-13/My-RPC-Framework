package cn.hlh.rpc.handler;

import cn.hlh.rpc.entity.RpcRequest;
import cn.hlh.rpc.entity.RpcResponse;
import cn.hlh.rpc.enumeration.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    //由于所有的调用请求使用的是相同的RequesrtHandler，所以可以使用单实例。
    private RequestHandler(){}
    public <T> RpcResponse<T> handle(RpcRequest request, Object service){
        RpcResponse<T> response = null;
        try {
            response = invokeMethod(request,service);
            logger.info("服务:{} 成功调用方法:{}", request.getInterfaceName(), request.getMethodName());
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("调用或发送时有错误发生：", e);
            e.printStackTrace();
        }
        return response;
    }
    private <T> RpcResponse invokeMethod(RpcRequest request, Object service) throws InvocationTargetException, IllegalAccessException {
        Method method;
        try {
            method = service.getClass().getMethod(request.getMethodName(), request.getParamType());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(e, ResponseCode.METHOD_NOT_FOUND);
        }
        return RpcResponse.success(method.invoke(service,request.getParameters()));
    }
}
