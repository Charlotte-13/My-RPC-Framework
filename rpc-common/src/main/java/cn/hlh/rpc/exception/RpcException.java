package cn.hlh.rpc.exception;

import cn.hlh.rpc.enumeration.EmRpcError;

/**
 * Rpc调用时异常
 */
public class RpcException extends RuntimeException {
    private EmRpcError rpcError;
    public RpcException(EmRpcError rpcError, String detail){
        super(rpcError.getMsg()+"："+detail);
        this.rpcError = rpcError;
    }

    public RpcException(String message, Throwable cause){
        super(message, cause);
    }

    public RpcException(EmRpcError rpcError){
        super(rpcError.getMsg());
        this.rpcError = rpcError;
    }
    public EmRpcError getRpcError() {
        return rpcError;
    }

    public void setRpcError(EmRpcError rpcError) {
        this.rpcError = rpcError;
    }
}
