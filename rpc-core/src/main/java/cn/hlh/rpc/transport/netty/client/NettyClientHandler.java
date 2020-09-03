package cn.hlh.rpc.transport.netty.client;

import cn.hlh.rpc.entity.RpcRequest;
import cn.hlh.rpc.entity.RpcResponse;
import cn.hlh.rpc.enumeration.ResponseCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    //当通道有读取事件时会触发该方法
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        try{
            if (rpcResponse.getResponseCode()==ResponseCode.HEART_BEAT && "ping".equals(rpcResponse.getData())){
                //如果是服务端的心跳包，则回复
                logger.info("客户端收到心跳包，进行回复...");
                RpcRequest request = new RpcRequest();
                request.setHeartBeat(true);
                channelHandlerContext.writeAndFlush(request);
            }else {
                logger.info(String.format("客户端接收到消息: %s", rpcResponse.getResponseCode().getMsg()));
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                channelHandlerContext.channel().attr(key).set(rpcResponse);
                channelHandlerContext.channel().close();
            }
        }finally {
            ReferenceCountUtil.release(rpcResponse);
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }

}
