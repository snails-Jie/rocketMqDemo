package zhangjie.rocketmq.netty.time.sever;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 因为我们要忽略任何接受到的数据，而是在连接建立之后立即发送发送消息
 *  --> 所以不能使用channelRead()方法，应该重写channelActive()方法
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 1. 当连接建立并准备好产生流量时，channelActive()方法将被调用
     * 2. 要发送一条新的消息，需要分配一个新的缓冲区来包含消息
     *   2.1 通过ChannelHandlerContext.alloc()获取当前的ByteBufAllocator,并分配一个新的缓冲区
     * 3. 以前在NIO中发送消息之前调用java.nio.ByteBuffer.flip()
     *   3.1 ByteBuf 没有这样的方法，因为它有两个指针，一个用于读操作，另一个用于写操作
     *   3.2 当向ByteBuf写东西时，写入索引（writer index）会增加，而读入索引(reader index)不会改变
     *   3.3 reader index和writer index分别代表消息的开始和结束地点
     *     --> NIO bUffer并没有提供一个干净的方法，在不调用flip方法的情况下，就可以算出消息内容的开始和结束位置
     *      --> netty 对不同的操作类型有不同的指针，不需要flip
     * 4. ChannelHandlerContext.write()和writeAndFlush()方法返回一个ChannelFuture
     *   4.1 在Netty中所有的操作都是异步的，例如下面的操作可能会在消息发送之前就关闭连接了
     *       Channel ch = ...;
     *       ch.writeAndFlush(message);
     *       ch.close();
     *   4.2 需要在ChannelFuture完成之后调用close()方法
     *      --> 即由write()方法返回的ChannelFutrue完成后，它在写操作后会通知它的监听器
     *   4.3 close()方法也可能不会立即关闭连接，它返回的是ChannelFuture
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final ByteBuf time = ctx.alloc().buffer(4);
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

        final ChannelFuture f = ctx.writeAndFlush(time);
        /**
         *  可以使用预设的监听器来简化代码
         *  f.addListener(ChannelFutureListener.CLOSE);
         */
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                assert f == channelFuture;
                ctx.close();
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
