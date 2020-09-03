package cn.hlh.rpc.transport;

import cn.hlh.rpc.entity.RpcRequest;

public interface RpcClient {
    Object sendRequest(RpcRequest request);
}
