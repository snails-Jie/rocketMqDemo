package zhangjie.rocketmq.netty.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * 1.ChannelInboundHandlerAdapter是ChannelInboundHandler的实现
 *   1.1 ChannelInboundHandler提供了各种事件处理方法
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter{

    /**
     * 1.每当从客户端接收到新的数据时，该方法就会和接收的消息一起被调用
     *  1.1 接收到的消息的类型是ByteBuf
     * 2. 为了实现DISCARD协议，处理程序必须忽略接收到的消息
     *  2.1 ByteBuf是一个引用计数对象，必须通过release()显式释放
     *  2.2 释放传递给处理程序的任何引用计数对象是处理程序的责任，通常channelRead()实现为:
     *     @Override
     *      public void channelRead(ChannelHandlerContext ctx, Object msg) {
     *           try {
     *               // Do something with msg
     *           } finally {
     *               ReferenceCountUtil.release(msg);
     *          }
     *       }
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        //丢弃收到的数据
//        ((ByteBuf)msg).release();

//        ByteBuf in = (ByteBuf) msg;
//        try {
//            while (in.isReadable()) { // (1)
//                System.out.print((char) in.readByte());
//                System.out.flush();
//            }
//        } finally {
//            ReferenceCountUtil.release(msg); // (2)
//        }

        /**
         *  1.ChannelHandlerContext对象提供了各种操作，使你能够出发各种I/O事件和操作
         *   1.1 ctx.write(Object) 没有将消息写到线上，它在内部进行缓冲
         *    -->可以使用ctx.writeAndFlush(msg)
         */
        ctx.write(msg); // (1)
        ctx.flush(); // (2)
    }

    /**
     * 1.当netty由于I/O错误或处理事件时的异常被Netty提出，exceptionCaught()事件处理方法就会被调用
     * 2.在大多数情况下，捕获到的异常应该被记录下来，并在这里关闭它的相关通道
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        /** 当出现异常时，关闭连接 */
        cause.printStackTrace();
        ctx.close();
    }
}
