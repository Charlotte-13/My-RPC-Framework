package cn.hlh.rpc.codec;

import cn.hlh.rpc.enumeration.PackageType;
import cn.hlh.rpc.serializer.CommonSerializer;
import cn.hlh.rpc.entity.RpcRequest;
import cn.hlh.rpc.entity.RpcResponse;
import cn.hlh.rpc.enumeration.EmRpcError;
import cn.hlh.rpc.exception.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CommonDecoder extends ReplayingDecoder {
    private static final Logger logger = LoggerFactory.getLogger(CommonDecoder.class);
    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magic = byteBuf.readInt();
        if(magic!=MAGIC_NUMBER){
            logger.error("不识别的协议类型: {}", magic);
            throw new RpcException(EmRpcError.UNKNOWN_PROTOCOL);
        }
        int packageTypeCode = byteBuf.readInt();
        Class<?> packageType;
        if(packageTypeCode == PackageType.REQUESR_PACK.getCode()){
            packageType = RpcRequest.class;
        }else if(packageTypeCode == PackageType.RESPONSE_PACK.getCode()){
            packageType = RpcResponse.class;
        }else {
            logger.error("不识别的数据包类型: {}", packageTypeCode);
            throw new RpcException(EmRpcError.UNKNOWN_PACKAGE_TYPE);
        }
        int serializerCode = byteBuf.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if(serializer==null){
            logger.error("不识别的序列化器: {}", serializerCode);
            throw new RpcException(EmRpcError.UNKNOWN_SERIALIZER);
        }
        int dataLength = byteBuf.readInt();
        byte[] bytes = new byte[dataLength];
        byteBuf.readBytes(bytes);
        Object data = serializer.deserialize(bytes,packageType);
        list.add(data);
    }
}
