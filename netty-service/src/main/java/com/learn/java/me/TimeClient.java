package com.learn.java.me;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.Date;
import java.util.List;

/**
 * 来自netty 官网 docs
 */
public class TimeClient {
    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8080;
        if (args.length > 0) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // Bootstrap类似于ServerBootstrap，不同之处在于它适用于非服务器通道，如客户端或无连接通道。
            Bootstrap b = new Bootstrap(); // (1)
            // 如果您只指定一个EventLoopGroup，它将同时用作老板组和工作组。但是，boss worker不用于客户端。
            b.group(workerGroup); // (2)
            // NioSocketChannel被用来创建客户端通道，而不是NioServerSocketChannel。
            b.channel(NioSocketChannel.class); // (3)
            /*
            NioServerSocketChannel服务器用来监听新连接请求的通道，可以看作是“父”通道。
            NioSocketChannel 每当一个新的客户端连接被接受时，都会创建一个新的 NioSocketChannel 实例来处理这个连接，这些可以看作是“子”通道。
            在客户端，情况有所不同。客户端使用 Bootstrap 来配置和启动一个单一的 SocketChannel，用于发起对外的连接请求。
            因为客户端没有监听新的连接请求的需求，也就不存在“父”通道的概念。
             */
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new TimeClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}

@Deprecated
class TimeClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg; // (1)
        try {
            // TODO 此处代码可能会抛出 IndexOutOfBoundsException 异常，在流式处理中介绍，本地测试为什么不容易出现
            // 因为int类型数据很小，只有32bit，MTU一般是1500字节，但是随着流量的增加也是有可能的。
            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            // 只读取一次即关闭client
            ctx.close();
        } finally {
            m.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}


/**
 * 碎片化解决方案1
 * ChannelHandler有两个生命周期侦听器方法：handlerAdded（）和handlerRemoved（）。只要不长时间阻塞，就可以执行任意的（去）初始化任务。
 */
class TimeClientHandlerNotRecommend extends ChannelInboundHandlerAdapter {
    private ByteBuf buf;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        buf = ctx.alloc().buffer(4); // (1)
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        buf.release(); // (1)
        buf = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        buf.writeBytes(m); // (2)
        m.release();

        if (buf.readableBytes() >= 4) { // (3)
            long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

/**
 * 解决方案2
 * 1 ByteToMessageDecoder是ChannelInboundHandler的一个实现，它可以很容易地处理碎片问题。
 * 2 每当接收到新数据时，ByteToMessageDecoder使用内部维护的累积缓冲区调用decode（）方法。
 * 3 当累积缓冲区中没有足够的数据时，Decode() 可以决定不向out添加任何数据。ByteToMessageDecoder将在接收到更多数据时再次调用decode() 。
 * 4 如果decode() 向out添加一个对象，则表示解码器成功解码了一条消息。ByteToMessageDecoder将丢弃累积缓冲区的读部分。请记住，
 *   您不需要解码多个消息。ByteToMessageDecoder将继续调用decode() 方法，直到它没有向out添加任何内容。
 */
class TimeDecoder extends ByteToMessageDecoder { // (1)
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        if (in.readableBytes() < 4) {
            return; // (3)
        }

        out.add(in.readBytes(4)); // (4)
    }
}