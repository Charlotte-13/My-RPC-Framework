package cn.hlh.rpc.transport.socket.client;

import cn.hlh.rpc.entity.RpcRequest;
import cn.hlh.rpc.transport.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);
    private String host;
    private int port;
    public SocketClient(String host, int port){
        this.host = host;
        this.port = port;
    }
    public Object sendRequest(RpcRequest request){
        //对于放在括号中的资源，try块退出时，会自动调用res.close()方法，关闭资源。
        try(Socket socket = new Socket(host, port)){
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(request);
            out.flush();
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("调用时有错误发生：",e);
            return null;
        }
    }
}
