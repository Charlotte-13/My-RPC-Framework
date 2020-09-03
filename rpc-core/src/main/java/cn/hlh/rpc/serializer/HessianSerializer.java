package cn.hlh.rpc.serializer;

import cn.hlh.rpc.enumeration.EmRpcError;
import cn.hlh.rpc.enumeration.SerializerCode;
import cn.hlh.rpc.exception.RpcException;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements CommonSerializer {
    private static final Logger logger = LoggerFactory.getLogger(HessianSerializer.class);
    @Override
    public <T> byte[] serialize(T object) {
        HessianOutput hessianOutput = null;
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            hessianOutput = new HessianOutput(os);
            hessianOutput.writeObject(object);
            return os.toByteArray();
        } catch (IOException e) {
            logger.error("序列化时有错误发生:", e);
            throw new RpcException(EmRpcError.FAILED_TO_SERIALIZE);
        }finally {
            if(hessianOutput!=null){
                try {
                    hessianOutput.close();
                } catch (IOException e) {
                    logger.error("关闭流时有错误发生:", e);
                }
            }
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> classType) {
        HessianInput hessianInput = null;
        try(ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            hessianInput = new HessianInput(is);
            return (T) hessianInput.readObject(classType);
        } catch (IOException e) {
            logger.error("序列化时有错误发生:", e);
            throw new RpcException(EmRpcError.FAILED_TO_DESERIALIZE);
        }finally {
            if(hessianInput!=null){
                hessianInput.close();
            }
        }

    }

    @Override
    public int getCode() {
        return SerializerCode.Hessian.getCode();
    }
}
