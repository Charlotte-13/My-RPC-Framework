package cn.hlh.rpc.transport;

public interface RpcServer {
    void start();
    <T> void publishService(T service);
    void scanServicesByAnnotation(int port);
}
