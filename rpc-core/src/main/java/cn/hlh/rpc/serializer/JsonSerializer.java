package cn.hlh.rpc.serializer;

import cn.hlh.rpc.entity.RpcRequest;
import cn.hlh.rpc.enumeration.SerializerCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Json序列化器
 */
public class JsonSerializer implements CommonSerializer {
    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);
    private ObjectMapper mapper = new ObjectMapper();
    @Override
    public <T> byte[] serialize(T object) {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            logger.error("序列化时有错误发生: {}",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> classType) {
        try {
            T object = mapper.readValue(bytes,classType);
            if(object instanceof RpcRequest){
                object = (T) handleRequest(object);
            }
            return object;
        } catch (IOException e) {
            logger.error("反序列化时有错误发生: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 由于Json序列化时会丢失RpcRequest中的参数数组的类型信息，因此对其的反序列化需要单独处理
     * @param object
     * @return
     */
    private <T> RpcRequest handleRequest(T object) throws IOException {
        RpcRequest rpcRequest = (RpcRequest) object;
        for(int i=0; i<rpcRequest.getParamType().length; i++){
            Class<?> clazz = rpcRequest.getParamType()[i];
            //如果参数实例的类型不是对应参数类型的子类，则代表该参数丢失了类型信息，需要重新序列化
            if(!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())){
                byte[] bytes = mapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                rpcRequest.getParameters()[i] = mapper.readValue(bytes,clazz);
            }
        }
        return rpcRequest;
    }

    @Override
    public int getCode() {
        return SerializerCode.Json.getCode();
    }
}
