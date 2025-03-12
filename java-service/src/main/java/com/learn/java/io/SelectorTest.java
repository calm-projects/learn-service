package com.learn.java.io;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Slf4j
public class SelectorTest {
    private static final String HOST = "localhost";
    private static final int PORT = 8888;

    @Test
    public void server() {
        try (ServerSocketChannel ssc = ServerSocketChannel.open(); Selector selector = Selector.open()) {
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(HOST, PORT));
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer readBuff = ByteBuffer.allocate(1024);
            ByteBuffer writeBuff = ByteBuffer.allocate(128);
            writeBuff.put("hello client".getBytes());
            writeBuff.flip();

            while (!Thread.currentThread().isInterrupted()) {
                if (selector.select() > 0) { // 防止阻塞
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove(); // 必须移除

                        if (key.isAcceptable()) {
                            SocketChannel socketChannel = ssc.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            log.info("新连接来自: {}", socketChannel.getRemoteAddress());
                        } else if (key.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            readBuff.clear();
                            int read = socketChannel.read(readBuff);
                            if (read == -1) { // 客户端关闭连接
                                key.cancel();
                                socketChannel.close();
                                continue;
                            }
                            readBuff.flip();
                            log.info("从客户端接受到的数据: {}", new String(readBuff.array(), 0, read, StandardCharsets.UTF_8));
                            key.interestOps(SelectionKey.OP_WRITE);
                        } else if (key.isWritable()) {
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            socketChannel.write(writeBuff);
                            writeBuff.rewind(); // 准备下一次写入
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("服务器错误", e);
        }
    }

    @Test
    public void client() {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress(HOST, PORT));
            ByteBuffer writeBuffer = ByteBuffer.allocate(32);
            ByteBuffer readBuffer = ByteBuffer.allocate(32);
            writeBuffer.put("hello server".getBytes());
            writeBuffer.flip();
            socketChannel.write(writeBuffer); // 发送初始消息

            // 简单地等待服务器响应并打印
            readBuffer.clear();
            int read = socketChannel.read(readBuffer);
            readBuffer.flip();
            log.info("从服务端接收到的数据: {}", new String(readBuffer.array(), 0, read, StandardCharsets.UTF_8));

        } catch (IOException e) {
            log.error("客户端错误", e);
        }
    }
}