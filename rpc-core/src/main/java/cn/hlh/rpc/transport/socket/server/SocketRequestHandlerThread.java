package cn.hlh.rpc.transport.socket.server;

import cn.hlh.rpc.entity.RpcRequest;
import cn.hlh.rpc.entity.RpcResponse;
import cn.hlh.rpc.handler.RequestHandler;
import cn.hlh.rpc.provider.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketRequestHandlerThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SocketRequestHandlerThread.class);
    private ServiceProvider serviceProvider;
    private Socket socket;
    private RequestHandler requestHandler;
    public SocketRequestHandlerThread(ServiceProvider serviceProvider, Socket socket, RequestHandler requestHandler){
        this.serviceProvider = serviceProvider;
        this.socket = socket;
        this.requestHandler = requestHandler;
    }
    @Override
    public void run() {
        try(ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest request = (RpcRequest)in.readObject();
            String interfaceName = request.getInterfaceName();
            Object service = serviceProvider.getService(interfaceName);
            Object result = requestHandler.handle(request, service);
            out.writeObject(RpcResponse.success(result));
            out.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("调用或发送时有错误发生：",e);
        }
    }
}
