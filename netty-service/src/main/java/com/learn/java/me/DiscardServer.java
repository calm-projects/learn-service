package com.learn.java.me;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * Discards any incoming data.
 * 来自netty 官网 docs
 */
public class DiscardServer {

    private int port;

    public DiscardServer(int port) {
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
                            // DiscardServerHandler需要我们自己实现
                            ch.pipeline().addLast(new DiscardServerHandler());
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
     * 1. 打开终端，使用telnet localhost 8080 或者使用 nc 连接服务端
     * 2. 连接成功后在client输入内容服务端即会打印客户端输入的内容
     */
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new DiscardServer(port).run();
    }
}

/**
 * Handles a server-side channel.
 */
// ChannelInboundHandler提供了各种可以覆盖的事件处理程序方法。
// 直接继承ChannelInboundHandlerAdapter即可
class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)

    // 当从客户端接收到新数据时，将使用接收到的消息调用此方法。
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        // 直接丢弃，不处理
        // Discard the received data silently. ByteBuf是一个引用计数的对象，必须通过release（）方法显式释放
        // ((ByteBuf) msg).release(); // (3)

        // 直接打印
        // handlerPrint(msg);

        // 输出到客户端, 我们没有像在DISCARD示例中那样释放接收到的消息。这是因为当它被写入到网络中时，Netty会为您释放它。
        ctx.writeAndFlush(msg);
    }

    private void handlerPrint(Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            /*while (in.isReadable()) { // 可以直接使用下面的代码替换
                System.out.print((char) in.readByte());
                System.out.flush();
            }*/
            System.out.println(in.toString(CharsetUtil.UTF_8));
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
