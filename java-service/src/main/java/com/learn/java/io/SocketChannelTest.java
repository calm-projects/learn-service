package com.learn.java.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class SocketChannelTest {

    private static final String HOST = "localhost";
    private static final int PORT = 8888;

    /**
     * 测试 Socket 服务器
     */
    @Test
    public void testSocketServer() {
        final String greeting = "Hello I must be going.\r\n";
        ByteBuffer buffer = ByteBuffer.wrap(greeting.getBytes(StandardCharsets.UTF_8));

        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(PORT));

            System.out.println("Server started on port " + PORT);

            while (true) {
                SocketChannel sc = ssc.accept();
                if (sc != null) {
                    try (sc) { // 自动关闭 SocketChannel
                        System.out.println("Incoming connection from: " + sc.getRemoteAddress());
                        buffer.rewind(); // 重置缓冲区位置
                        int bytesWritten = sc.write(buffer);
                        if (bytesWritten > 0) {
                            System.out.println("Sent " + bytesWritten + " bytes to client.");
                        } else {
                            System.err.println("Failed to write data to client.");
                        }
                    } catch (IOException e) {
                        System.err.println("Error handling client connection: " + e.getMessage());
                    }
                } else {
                    // 避免忙等待，休眠一段时间
                    TimeUnit.SECONDS.sleep(1);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Server encountered an error: " + e.getMessage());
        }
    }

    /**
     * 测试 Socket 客户端
     */
    @Test
    public void testSocketClient() {
        try (SocketChannel sc = SocketChannel.open(new InetSocketAddress(HOST, PORT))) {
            sc.configureBlocking(false);

            // 等待连接完成
            while (!sc.finishConnect()) {
                System.out.println("Waiting for connection...");
                TimeUnit.MILLISECONDS.sleep(500); // 避免忙等待
            }

            System.out.println("Connected to server at " + HOST + ":" + PORT);
            //  睡眠1s钟接受数据
            TimeUnit.SECONDS.sleep(1);

            // 准备接收数据的缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = sc.read(buffer);

            if (bytesRead > 0) {
                buffer.flip(); // 切换到读模式
                String response = new String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8);
                System.out.println("Received from server: " + response);
            } else {
                System.out.println("No data received from server.");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Client encountered an error: " + e.getMessage());
        }
    }
}