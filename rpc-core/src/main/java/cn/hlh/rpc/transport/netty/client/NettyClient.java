package cn.hlh.rpc.transport.netty.client;

import cn.hlh.rpc.codec.CommonDecoder;
import cn.hlh.rpc.codec.CommonEncoder;
import cn.hlh.rpc.entity.RpcRequest;
import cn.hlh.rpc.entity.RpcResponse;
import cn.hlh.rpc.enumeration.EmRpcError;
import cn.hlh.rpc.enumeration.ResponseCode;
import cn.hlh.rpc.enumeration.SerializerCode;
import cn.hlh.rpc.exception.RpcException;
import cn.hlh.rpc.factory.SingletonFactory;
import cn.hlh.rpc.registry.ServiceDiscoverer;
import cn.hlh.rpc.registry.nacos.NacosServiceDiscoverer;
import cn.hlh.rpc.registry.nacos.NacosServiceRegistrar;
import cn.hlh.rpc.registry.ServiceRegistrar;
import cn.hlh.rpc.serializer.CommonSerializer;
import cn.hlh.rpc.transport.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NettyClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    //将客户端的事件循环组和启动器设为静态，从而不会因为创建多个client实例而出现多个事件循环组。
    private static final Bootstrap bootStrap;
    private static final EventLoopGroup group;;
    private final SerializerCode serializerCode;
    private final ServiceRegistrar serviceRegistrar;
    private final ServiceDiscoverer serviceDiscoverer;
    //防止客户端对同一个服务端重复建立channel，key为IP地址+端口号
    private Map<String,Channel> channelMap = new ConcurrentHashMap<>();
    public NettyClient(SerializerCode serializerCode){
        this.serializerCode = serializerCode;
        this.serviceRegistrar = SingletonFactory.getInstance(NacosServiceRegistrar.class);
        this.serviceDiscoverer = new NacosServiceDiscoverer();
    }
    static {
        //客户端只需要一个事件循环组
        group = new NioEventLoopGroup();
        //创建客户端启动对象，注意和服务端区分
        bootStrap = new Bootstrap();
        //设置相关参数
        bootStrap.group(group)//设置线程组
                .channel(NioSocketChannel.class)//设置客户端通道实现类
                //连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                //是否开启 TCP 底层心跳机制
                .option(ChannelOption.SO_KEEPALIVE, true)
                //TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                .option(ChannelOption.TCP_NODELAY, true);
    }
    @Override
    public Object sendRequest(RpcRequest request) {
        Channel channel = getChannel(request);
        //向服务端发送请求，并监听请求是否成功发送
        if(channel!=null){
            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        logger.info(String.format("客户端发送消息: %s", request.getMethodName()));
                    }else {
                        logger.error("发送消息时有错误发生: ",channelFuture.cause());
                    }
                }
            });
            //同步调用
            /*//对关闭通道进行监听
            channel.closeFuture().sync();
            System.out.println(channel.closeFuture().isSuccess());
            //通过 AttributeKey 的方式阻塞获得返回结果
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            RpcResponse rpcResponse = channel.attr(key).get();*/
            //异步调用
            RequestCallBack callBack = new RequestCallBack(request);
            return callBack;
        }else {
            return RpcResponse.fail(new RpcException(EmRpcError.SERVICE_NOT_FOUND), ResponseCode.CLASS_NOT_FOUND);
        }
    }

    private Channel getChannel(RpcRequest request) {
        boolean connectFlag = false;
        while (!connectFlag){
            InetSocketAddress inetSocketAddress = serviceDiscoverer.lookupService(request.getInterfaceName());
            String key = inetSocketAddress.getAddress().getHostAddress()+":"+inetSocketAddress.getPort();
            if(channelMap.containsKey(key)){
                Channel channel = channelMap.get(key);
                if(channel!=null&&channel.isActive()){
                    return channel;
                }
            }
            connectFlag = doConnect(inetSocketAddress,serializerCode,1);
            if(connectFlag){
                return channelMap.get(key);
            }
        }
        return null;
    }

    /**
     * 该方法用于和服务端建立连接，若连接失败，10秒后会重连，最多重连10次
     * @param inetSocketAddress
     * @param serializerCode
     * @param connectCount
     * @return true表示建立连接成功，false表示建立连接失败
     */
    private boolean doConnect(InetSocketAddress inetSocketAddress,SerializerCode serializerCode,int connectCount) {
        bootStrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new CommonEncoder(CommonSerializer.getByCode(serializerCode.getCode())))
                        .addLast(new CommonDecoder())
                        .addLast(new NettyClientHandler());//设置pipeline的数据处理器
            }
        });
        ChannelFuture future = null;
        try {
            future = bootStrap.connect(inetSocketAddress.getHostName(),inetSocketAddress.getPort()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    final EventLoop eventLoop = channelFuture.channel().eventLoop();
                    if(!channelFuture.isSuccess()){
                        if(connectCount<=10){
                            logger.warn("客户端已启动，与服务端建立连接失败,10s之后尝试重连!");
                            eventLoop.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    doConnect(inetSocketAddress,serializerCode,connectCount+1);
                                }
                            }, 10,TimeUnit.SECONDS);
                        }else {
                            logger.error("客户端对该服务器重连超过10次，该服务器被认定失效！");
                            return;
                        }
                    }else {
                        logger.info("客户端连接到服务器 {}:{}",inetSocketAddress.getAddress().getHostAddress(),inetSocketAddress.getPort());
                    }
                }
            }).sync();
        } catch (InterruptedException e) {
            logger.error("连接服务器时有错误发生：",e);
        }
        if(future.isSuccess()){
            String key = inetSocketAddress.getAddress().getHostAddress()+":"+inetSocketAddress.getPort();
            channelMap.put(key, future.channel());
            return true;
        }else {
            return false;
        }
    }
}
