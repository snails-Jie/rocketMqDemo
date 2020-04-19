package zhangjie.rocketmq.netty.time.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 在Netty中，服务器和客户端最大也是唯一的区别就是使用了不同的Bootstrap和Channel实现
 * 1. Bootstrap 针对非服务器Channel，如客户端或无连接通道
 * 2. 如果你只指定一个EventLoopGroup，那么将同时作为boss Group 和 worker Group
 *    --> 但 boss worker并不做用于客户端
 * 3. NioSocketChannel被用于来创建一个客户端的Channel
 * 4. 这里没有childOption(),与ServerBootstrap不同，因为客户端的SocektChannel没有父节点
 *
 */
public class TimeClient {
    public static void main(String[] args) throws Exception {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try{
            Bootstrap b = new Bootstrap();
            b.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TimeClientHandler());
                        }
                    });
            ChannelFuture f = b.connect(host,port).sync();
            f.channel().closeFuture().sync();

        }finally {
            workGroup.shutdownGracefully();
        }
    }
}
