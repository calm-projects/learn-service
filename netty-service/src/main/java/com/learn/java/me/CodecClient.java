package com.learn.java.me;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Random;

public class CodecClient {
    public static void main(String[] args) throws InterruptedException {
        String host = "localhost";
        int port = 8080;
        if (args.length > 0) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        NioEventLoopGroup works = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(works)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    System.out.println("connect server ...");
                                    Random r = new Random();
                                    char c = 'a';
                                    ByteBuf buffer = ctx.alloc().buffer();
                                    for (int i = 0; i < 10; i++) {
                                        byte length = (byte) (r.nextInt(16) + 1);
                                        buffer.writeByte(length);
                                        for (int j = 1; j <= length; j++) {
                                            buffer.writeByte((byte) c);
                                        }
                                        c++;
                                    }
                                    ctx.writeAndFlush(buffer);
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    super.exceptionCaught(ctx, cause);
                                }
                            });
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            works.shutdownGracefully();
        }
    }
}
