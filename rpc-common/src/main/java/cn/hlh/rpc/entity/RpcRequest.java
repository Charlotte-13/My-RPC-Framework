package cn.hlh.rpc.entity;

import java.io.Serializable;

/**
 * Rpc中传递给服务端的请求
 */
public class RpcRequest implements Serializable {
    //待调用接口名称
    private String interfaceName;

    //待调用方法名称
    private String methodName;

    //调用方法的参数
    private Object[] parameters;

    //调用方法的参数类型
    private Class<?>[] paramType;

    //心跳包标识
    private boolean heartBeat;

    //请求id，用于和调用响应匹配，实现异步调用
    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(boolean heartBeat) {
        this.heartBeat = heartBeat;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParamType() {
        return paramType;
    }

    public void setParamType(Class<?>[] paramType) {
        this.paramType = paramType;
    }
}
