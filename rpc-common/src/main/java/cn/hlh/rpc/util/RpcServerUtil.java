package cn.hlh.rpc.util;

import cn.hlh.rpc.enumeration.EmRpcError;
import cn.hlh.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class RpcServerUtil {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerUtil.class);
    /**
     *获取当前服务器的IP地址
     * @return
     */
    public static InetAddress getCurrentIp(){
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
                        return ia;
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("获取服务端IP地址时有错误发生：",e);
            throw new RpcException(EmRpcError.FAILED_TO_GET_SERVER_IP);
        }
        return null;
    }
}
