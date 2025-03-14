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
public class TimeServer {

    private int port;

    public TimeServer(int port) {
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
                            ch.pipeline().addLast(new TimeServerHandler());
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

        new TimeServer(port).run();
    }
}

class TimeServerHandler extends ChannelInboundHandlerAdapter {

    // 当建立连接并准备生成流量时，调用的是channelActive（）方法
    @Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
        System.out.println(ctx.channel().remoteAddress() + " connected");
        // 分配一个byteBuf，netty
        final ByteBuf time = ctx.alloc().buffer(4); // (2)
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

        /*
            以前不是在NIO中发送消息之前调用java.nio.ByteBuffer.flip（）吗？ByteBuf没有这样的方法，因为它有两个指针；
        一个用于读操作，另一个用于写操作。当向ByteBuf写入内容时，写入器索引增加，而读取器索引不变。reader索引和writer
        索引分别表示消息的开始和结束位置。
            ChannelFuture表示一个尚未发生的I/O操作。这意味着，任何请求的操作都可能尚未执行，因为所有操作在Netty中都是异步的。
            因此，您需要在ChannelFuture完成后调用close（）方法（该方法由write（）方法返回），并在写操作完成时通知侦听器。请注意，
        close（）也可能不会立即关闭连接，它返回一个ChannelFuture。
         */
        final ChannelFuture f = ctx.writeAndFlush(time); // (3)
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                assert f == future;
                ctx.close();
            }
        }); // (4)
        // 可以使用更简单的操作 f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
