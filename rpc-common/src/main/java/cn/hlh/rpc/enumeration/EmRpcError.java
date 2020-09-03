package cn.hlh.rpc.enumeration;

public enum EmRpcError implements CommonCode{
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE(10001,"服务未实现任何接口"),
    SERVICE_NOT_FOUND(10002,"找不到服务"),
    UNKNOWN_PROTOCOL(10003,"不识别的协议类型"),
    UNKNOWN_PACKAGE_TYPE(10004,"不识别的数据包类型"),
    UNKNOWN_SERIALIZER(10005,"不识别的序列化器"),
    FAILED_TO_CONNECT_TO_SERVICE_REGISTRY(10006,"无法连接到注册中心"),
    REGISTER_SERVICE_FAILED(10007,"注册服务失败"),
    FAILED_TO_GET_SERVER_IP(10008,"获取服务端IP地址失败"),
    SERVICE_SCAN_PACKAGE_NOT_FOUND(10009,"找不到服务扫描注解"),
    FAILED_TO_GET_CLASS_FROM_SERVICE_PACKAGE(10010,"获取指定包的类失败"),
    FAILED_TO_SERIALIZE(10011,"序列化失败"),
    FAILED_TO_DESERIALIZE(10012,"反序列化失败"),
    UNKNOWN_ERROR(10013,"未知错误");
    private int errCode;
    private String errMsg;

    private EmRpcError(int errCode, String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
    @Override
    public int getCode() {
        return errCode;
    }

    @Override
    public String getMsg() {
        return errMsg;
    }

    @Override
    public CommonCode setMsg(String errorMsg) {
        this.errMsg = errorMsg;
        return this;
    }
}
