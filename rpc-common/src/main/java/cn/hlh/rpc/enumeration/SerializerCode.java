package cn.hlh.rpc.enumeration;

public enum SerializerCode implements CommonCode {
    Kryo(0,"Kryo序列化器"),
    Json(1,"Json序列化器"),
    ProtoBuf(2,"ProtoBuf序列化器"),
    Hessian(3,"Hessian序列化器");

    private int serializerCode;
    private String serializerMsg;

    private SerializerCode(int serializerCode,String serializerMsg){
        this.serializerCode = serializerCode;
        this.serializerMsg = serializerMsg;
    }
    @Override
    public int getCode() {
        return serializerCode;
    }

    @Override
    public String getMsg() {
        return serializerMsg;
    }

    @Override
    public CommonCode setMsg(String serializerMsg) {
        this.serializerMsg = serializerMsg;
        return this;
    }
}
