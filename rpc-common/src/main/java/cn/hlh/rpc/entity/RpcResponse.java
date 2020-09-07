package cn.hlh.rpc.entity;

import cn.hlh.rpc.enumeration.ResponseCode;
import cn.hlh.rpc.enumeration.CommonCode;

import java.io.Serializable;

/**
 * Rpc中返回给调用端的响应
 * @param <T>
 */
public class RpcResponse<T> implements Serializable {
    //响应状态码
    private CommonCode responseCode;
    //响应数据或异常信息
    private T data;
    //指明是对哪个请求的响应
    private String requestId;

    public static <T> RpcResponse<T> success(T data){
        RpcResponse<T> response = new RpcResponse<>();
        response.setResponseCode(ResponseCode.SUCCESS);
        response.setData(data);
        return response;
    }

    public static RpcResponse<String> heartBeat(){
        RpcResponse<String> response = new RpcResponse<>();
        response.setResponseCode(ResponseCode.HEART_BEAT);
        response.setData("ping");
        return response;
    }

    public static <T> RpcResponse<T> fail(T exception,CommonCode responseCode){
        RpcResponse<T> response = new RpcResponse<>();
        response.setResponseCode(responseCode);
        response.setData(exception);
        return response;
    }

    public CommonCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(CommonCode responseCode) {
        this.responseCode = responseCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
