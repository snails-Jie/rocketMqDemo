package zhangjie.rocketmq.netty.time.sever;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 1. NioEventLoopGroup 是一个处理I/O操作的多线程事件循环
 *   1.1 Netty为不同类型的传输器提供了多种EventLoopGroup实现
 *
 */
public class TimeServer {
    private int port;

    public TimeServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        /**
         * boss：接受一个传入的连接
         * worker: 一旦boss接受了连接注册到worker，就会处理接受的连接的流量
         * 使用多少线程以及如何将它们映射到创建的Channels上，取决于EventLoopGroup实现，甚至于可以通过构造函数配置
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            /**
             * 1. ServerBootstrap是一个设置服务器的帮助类
             * 2. NioServerSocketChannel 该类用于实例化一个新的Channle来接受传入的连接
             * 3. 这里指定的handler将始终由新接受的Channel来评估
             *   3.1 ChannelInitializer 是一个特殊的handler, 旨在帮助用户配置一个新的Channel
             *   3.2 可能你想添加一些handler（如DiscardServerHandler等）来配置新Channel的ChannelPipeline来实现你的网络应用
             * 4. 可以设置一些特定于通道实现的参数
             *   4.1 TCP/IP服务器，可以设置socket选项，如tcpNoDelay和keepAlive
             *   4.2 请参考ChannelOption和具体的ChannelConfig实现的api 文档来接支持的ChannelOptions
             *   4.3 option()是指NioServerSocketChannel接受传入的连接
             *       childOption()是指父 ServerChannel接受的Channel，这里是NioServerSocketChannel
             */
            ServerBootstrap b = new ServerBootstrap(); //1
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class) //2
                    .childHandler(new ChannelInitializer<SocketChannel>() { //3
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TimeServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG,128) //4
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            /** 绑定并开始接受传入的连接 */
            ChannelFuture f = b.bind(port).sync();

            /**
             *  等待直到server socket关闭
             *  在这个例子中，这种情况不会发生，但你可以这样做，以优雅的方式关闭服务
             */
            f.channel().closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 9090;
        if(args.length > 0){
            port = Integer.parseInt(args[0]);
        }
        new TimeServer(port).run();
    }
}
