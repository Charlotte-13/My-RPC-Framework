package cn.hlh.rpc.serializer;

import cn.hlh.rpc.enumeration.SerializerCode;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtoBufSerializer implements CommonSerializer {
    private LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    @Override
    public <T> byte[] serialize(T object) {
        Class<T> classType = (Class<T>) object.getClass();
        Schema<T> schema = RuntimeSchema.getSchema(classType);
        byte[] data;
        try{
            data = ProtobufIOUtil.toByteArray(object, schema, buffer);
        }finally {
            buffer.clear();
        }
        return data;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> classType) {
        Schema<T> schema = RuntimeSchema.getSchema(classType);
        T object = schema.newMessage();
        ProtobufIOUtil.mergeFrom(bytes,object,schema);
        return object;
    }

    @Override
    public int getCode() {
        return SerializerCode.ProtoBuf.getCode();
    }
}
