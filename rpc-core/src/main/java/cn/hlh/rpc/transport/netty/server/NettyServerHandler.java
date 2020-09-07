package cn.hlh.rpc.transport.netty.server;

import cn.hlh.rpc.entity.RpcRequest;
import cn.hlh.rpc.entity.RpcResponse;
import cn.hlh.rpc.factory.SingletonFactory;
import cn.hlh.rpc.handler.RequestHandler;
import cn.hlh.rpc.provider.DefaultServiceProvider;
import cn.hlh.rpc.provider.ServiceProvider;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 最后一个入站处理器，作为业务处理器，实际接收 RpcRequest，并且在服务端执行调用，将调用结果返回封装成 RpcResponse 发送出去。
 * 自定义的handler需要继承netty规定好的某个HandlerAdapter
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    //服务端的调用请求处理器和服务注册表
    private static RequestHandler requestHandler;
    private static ServiceProvider serviceProvider;
    //该类第一次被加载时创建，保证处理器和注册表在服务端的唯一性
    public NettyServerHandler(){
        requestHandler = SingletonFactory.getInstance(RequestHandler.class);
        serviceProvider = SingletonFactory.getInstance(DefaultServiceProvider.class);
    }
    //读取客户端发送来数据的方法
    //ChannelHandlerContext是上下文对象，含有管道（pipeline）、通道（channel）、地址等信息
    //RpcRequest是客户端发送来的数据
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest request) throws Exception {
        try{
            if(request.isHeartBeat()){
                logger.info("服务器收到回复心跳包...");
                return;
            }
            logger.info("服务器收到调用请求：{}",request.getMethodName());
            String interfaceName = request.getInterfaceName();
            Object service = serviceProvider.getService(interfaceName);
            RpcResponse<Object> rpcResponse = requestHandler.handle(request, service);
            if(channelHandlerContext.channel().isActive()&&channelHandlerContext.channel().isWritable()){
                //write+flush，将调用方法后得到的返回数据写入缓存，并刷新到管道中。该数据是编码后发送的
                ChannelFuture future = channelHandlerContext.writeAndFlush(rpcResponse);
                //future.addListener(ChannelFutureListener.CLOSE);
            }else {
                logger.error("通道不可写");
            }
        }finally {
            ReferenceCountUtil.release(request);
        }
    }

    //处理异常，一般需要关闭通道
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }

    //对心跳检测进行特殊处理。
    //如果出现15秒的读写空闲则给客户端发送心跳包；若出现60秒读空闲则表示客户端连接超时，关闭连接
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if(state==IdleState.READER_IDLE){
                logger.info("长时间未收到客户端的心跳包回复，断开连接...");
                //超时关闭channel
                ctx.channel().close();
            }else if(state==IdleState.ALL_IDLE){
                logger.info("出现长时间读写空闲，向客户端发送心跳包");
                //发送心跳包
                ctx.channel().writeAndFlush(RpcResponse.heartBeat());
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
