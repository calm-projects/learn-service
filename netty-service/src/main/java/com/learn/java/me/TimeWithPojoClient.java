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
public class TimeWithPojoClient {
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
                    ch.pipeline()
                            .addLast(new TimeWithPojoDecoder())
                            .addLast(new TimeWithPojoClientHandler());
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


class UnixTime {

    private final long value;

    public UnixTime() {
        this(System.currentTimeMillis() / 1000L + 2208988800L);
    }

    public UnixTime(long value) {
        this.value = value;
    }

    public long value() {
        return value;
    }

    @Override
    public String toString() {
        return new Date((value() - 2208988800L) * 1000L).toString();
    }
}


class TimeWithPojoClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        UnixTime m = (UnixTime) msg;
        System.out.println(m);
        // 只读取一次即关闭client
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

class TimeWithPojoDecoder extends ByteToMessageDecoder { // (1)
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        if (in.readableBytes() < 4) {
            return;
        }

        out.add(new UnixTime(in.readUnsignedInt()));
    }
}