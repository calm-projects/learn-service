package com.learn.java.me;


import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class ChannelClientTest {
    private final static String host = "127.0.0.1";
    private final static int port = 8080;
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("client 连接 server ....");
                                    String msg = "hello server";
                                    // 如果不编码的话需要发送byteBuf tcp 发送的是字节
                                    // ByteBuf msg = Unpooled.copiedBuffer(msg.getBytes(StandardCharsets.UTF_8));
                                    ChannelFuture channelFuture = ctx.writeAndFlush(msg);
                                    // 添加发送监听
                                    channelFuture.addListener((ChannelFutureListener) future -> {
                                        System.out.println(future.isSuccess());
                                        if (!future.isSuccess()) {
                                            future.cause().printStackTrace();
                                        }
                                    });
                                    // 是否关闭连接
                                    //.addListener(ChannelFutureListener.CLOSE);
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println("client read ....");
                                    ByteBuf in = (ByteBuf) msg;
                                    try {
                                        System.out.println(in.toString(CharsetUtil.UTF_8));
                                    } finally {
                                        ReferenceCountUtil.release(msg);
                                    }
                                }
                            });
                        }
                    });
            bootstrap.connect(host, port).sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}