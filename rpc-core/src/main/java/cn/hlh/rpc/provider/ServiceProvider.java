package cn.hlh.rpc.provider;

/**
 * 服务注册表接口
 */
public interface ServiceProvider {
    <T> void register(T service);
    <T>T getService(String interfaceName);
}
