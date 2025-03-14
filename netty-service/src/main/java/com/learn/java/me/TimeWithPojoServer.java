package com.learn.java.me;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 来自netty 官网 docs
 */
public class TimeWithPojoServer {

    private int port;

    public TimeWithPojoServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        /*
         *  NioEventLoopGroup是一个处理I/O操作的多线程事件循环
         *  boss, 接受传入的连接。worker 处理已接受连接的流量.
         *  一旦boss接受连接并将接受的连接注册到worker，它将处理已接受连接的流量
         *  使用多少线程以及如何将它们映射到创建的通道取决于EventLoopGroup实现，
         *  甚至可以通过构造函数进行配置。
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // ServerBootstrap是一个设置服务器的助手类。
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    // 用于实例化一个新Channel以接受传入的连接
                    .channel(NioServerSocketChannel.class) // (3)
                    // ChannelInitializer是一个特殊的处理程序，用于帮助用户配置新通道。
                    // addLast 可以添加多个，SocketChannel实现类有 Epoll KQueue Nio
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    // TODO 先添加 TimeEncoder
                                    .addLast(new TimeEncoder())
                                    .addLast(new TimeWithPojoServerHandler());
                        }
                    })
                    /*
                     * 用来设置NioServerSocketChannel本身的选项。NioServerSocketChannel是服务器
                     * 用来监听传入连接请求的通道。通过调用option()，你可以指定一些适用于服务器通
                     * 道自身的参数，比如SO_BACKLOG，它决定了等待接受的连接队列的最大长度。
                     * childOption()：这个方法则是为由NioServerSocketChannel（即父通道）
                     * 所接受的新连接（也就是客户端连接）的通道设置选项。
                     */
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * 发送一个包含32位整数的消息，而不接收任何请求，并在消息发送后关闭连接。
     * 所有的server和client都是一样的基本，改变的只是pipe处理的Channel
     */
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new TimeWithPojoServer(port).run();
    }
}

class TimeWithPojoServerHandler extends ChannelInboundHandlerAdapter {

    // 当建立连接并准备生成流量时，调用的是channelActive（）方法
    @Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
        System.out.println(ctx.channel().remoteAddress() + " connected");
        ChannelFuture f = ctx.writeAndFlush(new UnixTime());
        f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

class TimeEncoder extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        UnixTime m = (UnixTime) msg;
        ByteBuf encoded = ctx.alloc().buffer(4);
        encoded.writeInt((int) m.value());
        ctx.write(encoded, promise); // (1)
    }
}