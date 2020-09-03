package cn.hlh.rpc.serializer;

/**
 * 序列化器通用接口
 */
public interface CommonSerializer {
    <T> byte[] serialize(T object);
    <T> T deserialize(byte[] bytes, Class<T> classType);
    int getCode();
    static CommonSerializer getByCode(int code){
        switch (code){
            case 0:
                return new KryoSerializer();
            case 1:
                return new JsonSerializer();
            case 2:
                return new ProtoBufSerializer();
            case 3:
                return new HessianSerializer();
            default:
                return null;
        }
    }
}
