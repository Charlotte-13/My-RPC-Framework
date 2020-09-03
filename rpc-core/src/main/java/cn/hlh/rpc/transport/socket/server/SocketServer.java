package cn.hlh.rpc.transport.socket.server;

import cn.hlh.rpc.factory.SingletonFactory;
import cn.hlh.rpc.handler.RequestHandler;
import cn.hlh.rpc.transport.AbstractRpcServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class SocketServer extends AbstractRpcServer {
    //处理远程请求的线程池
    private final ExecutorService threadPool;
    private static final int corePoolSize = 5;
    private static final int maximumPoolSize = 50;
    private static final int keepAliveTime = 60;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;
    private RequestHandler requestHandler = SingletonFactory.getInstance(RequestHandler.class);
    public SocketServer(){
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime, TimeUnit.SECONDS,workingQueue,threadFactory);
    }

    @Override
    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            logger.info("服务器正在启动...");
            Socket socket;
            //accept方法会阻塞直到有请求连接
            while ((socket = serverSocket.accept())!=null){
                logger.info("消费端连接：{}:{}",socket.getInetAddress(),socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(serviceProvider,socket,requestHandler));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("连接时有错误发生：",e);
        }
    }
}
