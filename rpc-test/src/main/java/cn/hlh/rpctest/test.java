package cn.hlh.rpctest;

import cn.hlh.rpc.util.RpcServerUtil;

public class test {
    public static void main(String[] args) {
        System.out.println(RpcServerUtil.getCurrentIp().getHostAddress());
    }
}
