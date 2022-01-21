package com.imooc.netty;/**
 * @Title: project
 * @Package * @Description:     * @author CodingSir
 * @date 2022/1/149:34
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.stereotype.Component;

/**
 * @author zkjy
 * @version 1.0
 * @description zkjy
 * @updateUser
 * @createDate 2022/1/14 9:34
 * @updateDate 2022/1/14 9:34
 **/
@Component
public class WSServer {
    //单例server
    private static class SingletionWSServer {
        static final WSServer INSTANCE = new WSServer();
    }

    private static WSServer getInstance() {
        return SingletionWSServer.INSTANCE;
    }

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap server;
    private ChannelFuture future;

    public WSServer() {
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        server = new ServerBootstrap();
        server.group(mainGroup, subGroup).channel(NioServerSocketChannel.class)
                .childHandler(new WSServerInitialzer());
    }

    public void start() {
        this.future = server.bind(8088);
        System.err.println("netty websocket server 启动完毕...");
    }
}
