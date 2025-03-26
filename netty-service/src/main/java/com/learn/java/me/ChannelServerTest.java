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

import java.nio.charset.StandardCharsets;

public class ChannelServerTest {
    private final static int port = 8080;
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                                @Override
                                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                    System.out.println("write--->3");
                                    ByteBuf in = (ByteBuf) msg;
                                    System.out.println(in.toString(CharsetUtil.UTF_8));
                                    in.readerIndex(0);
                                    in.writeBytes("server reply: hello client".getBytes(StandardCharsets.UTF_8));
                                    System.out.println(msg.getClass().getName());
                                    super.write(ctx, msg, promise);
                                }
                            });

                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println("read--->1");
                                    ByteBuf in = (ByteBuf) msg;
                                    System.out.println(in.toString(CharsetUtil.UTF_8));
                                    // 需要将读索引重新设置为0
                                    in.readerIndex(0);
                                    // 如果此处不写出，其他Channel不执行
                                    super.channelRead(ctx, msg);
                                }
                            });
                            ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                                @Override
                                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                    System.out.println("write--->4");
                                    ByteBuf in = (ByteBuf) msg;
                                    System.out.println(in.toString(CharsetUtil.UTF_8));
                                    in.readerIndex(0);
                                    super.write(ctx, msg, promise);
                                }
                            });
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println("read--->2");
                                    ByteBuf in = (ByteBuf) msg;
                                    System.out.println(in.toString(CharsetUtil.UTF_8));
                                    in.readerIndex(0);
                                    /*
                                        写此代码，出站和入站可以随便写，都可以运行,但是数据不会写到client.
                                        super.channelRead(ctx, msg);
                                        写此代码，出站必须在入站之前添加，可以将数据写到client, 所以定义的时候最好将出站都定义在入站之前
                                        ctx.writeAndFlush(msg);
                                        这里具体的需要源码分析
                                     */
                                    ctx.writeAndFlush(msg); // 确保数据被写回客户端
                                }
                            });
                            ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                                @Override
                                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                    System.out.println("write--->5");
                                    ByteBuf in = (ByteBuf) msg;
                                    System.out.println(in.toString(CharsetUtil.UTF_8));
                                    in.readerIndex(0);
                                    super.write(ctx, msg, promise);
                                }
                            });
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.bind(port).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}