package cn.hlh.rpc.transport.netty.server;

import cn.hlh.rpc.codec.CommonDecoder;
import cn.hlh.rpc.codec.CommonEncoder;
import cn.hlh.rpc.enumeration.SerializerCode;
import cn.hlh.rpc.factory.SingletonFactory;
import cn.hlh.rpc.hook.ShutdownHook;
import cn.hlh.rpc.serializer.CommonSerializer;
import cn.hlh.rpc.transport.AbstractRpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class NettyServer extends AbstractRpcServer {
    private int port;
    private CommonSerializer serializer;
    public NettyServer(int port, SerializerCode serializerCode){
        this.port = port;
        this.serializer = CommonSerializer.getByCode(serializerCode.getCode());
        scanServicesByAnnotation(port);
    }
    @Override
    public void start() {
        //bossGroup只处理连接请求，workerGroup负责对客户端方法调用请求进行处理；这两个group都进行无限循环
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            //创建服务端的启动对象
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //使用链式编程配置参数
            serverBootstrap.group(bossGroup,workerGroup)//设置两个线程组
                    .channel(NioServerSocketChannel.class)//设置服务端的通道实现为NioServerSocketChannel
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG,256)//设置线程队列里等待连接的请求个数
                    .option(ChannelOption.SO_KEEPALIVE,true)//设置保持连接活动状态为true
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //给workerGroup的EventLoop的pipeline设置处理器
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //设置用于心跳检测的handler，服务端每出现60秒的读空闲或15秒读写空闲就会触发该handler
                            pipeline.addLast(new IdleStateHandler(60,0,15, TimeUnit.SECONDS));
                            pipeline.addLast(new CommonEncoder(serializer));
                            pipeline.addLast(new CommonDecoder());
                            //指定该pipeline的数据处理器
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            //绑定端口并且同步，启动服务端，线程会阻塞在这一步的ChannelFuture对象上直到bind操作成功执行。
            // ChannelFuture对象涉及netty的异步模型。
            ChannelFuture future = serverBootstrap.bind(port).sync();
            SingletonFactory.getInstance(ShutdownHook.class).addClearAllHook();
            //对关闭通道进行监听，每个Channel对象都有唯一的CloseFuture，用来表示关闭的Future，
            //该步就是执行CloseFuturn的sync方法，会将当前线程阻塞在CloseFuture上，保证服务端Channel的正常运行。
            //当调用channel的close方法后，当前线程就会被唤醒，继续执行。
            future.channel().closeFuture().sync();
        }catch (InterruptedException e){
            logger.error("启动服务器时有错误发生: ", e);
            e.printStackTrace();
        }finally {
            //关闭
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
