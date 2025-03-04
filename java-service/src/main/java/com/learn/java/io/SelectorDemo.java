package com.learn.java.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class SelectorDemo {

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器已启动，监听端口: 8080");
        while (true) {
            // 阻塞等待事件发生
            int readyChannels = selector.select();
            if (readyChannels == 0) continue;

            // 获取所有已就绪的 SelectionKey
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                // 处理新连接事件
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                }

                // 处理可读事件
                if (key.isReadable()) {
                    handleRead(key);
                }

                // 移除当前处理的键
                keyIterator.remove();
            }
        }
    }

    /**
     * 处理新连接
     */
    private static void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        // 注册到 Selector，监听 OP_READ 事件
        socketChannel.register(selector, SelectionKey.OP_READ);

        System.out.println("新客户端连接: " + socketChannel.getRemoteAddress());
    }

    /**
     * 处理读事件
     */
    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = socketChannel.read(buffer);

        if (bytesRead > 0) {
            buffer.flip(); // 切换到读模式
            String message = new String(buffer.array(), 0, buffer.limit());
            System.out.println("收到消息: " + message);

            // 回显消息给客户端
            buffer.rewind(); // 重置位置
            socketChannel.write(buffer);

            buffer.clear(); // 清空缓冲区
        } else if (bytesRead == -1) {
            // 客户端关闭连接
            System.out.println("客户端断开连接: " + socketChannel.getRemoteAddress());
            socketChannel.close();
            key.cancel(); // 取消注册
        }
    }
}