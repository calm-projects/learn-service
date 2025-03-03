package com.learn.java.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

@Slf4j
public class PipeDemo {

    public static void main(String[] args) throws IOException, InterruptedException {
        // 创建一个管道
        Pipe pipe = Pipe.open();

        // 启动写线程
        Thread writerThread = new Thread(() -> {
            try {
                Pipe.SinkChannel sinkChannel = pipe.sink(); // 获取 SinkChannel 用于写入数据
                String message = "Hello from writer thread!";
                ByteBuffer buffer = ByteBuffer.allocate(1024);

                // 将字符串写入缓冲区
                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();

                // 将缓冲区内容写入管道
                while (buffer.hasRemaining()) {
                    sinkChannel.write(buffer);
                }
                System.out.println("Writer thread wrote: " + message);
            } catch (IOException e) {
                log.error("writer error:", e);
            }
        });

        // 启动读线程
        Thread readerThread = new Thread(() -> {
            try {
                Pipe.SourceChannel sourceChannel = pipe.source(); // 获取 SourceChannel 用于读取数据
                ByteBuffer buffer = ByteBuffer.allocate(1024);

                // 从管道中读取数据
                int bytesRead = sourceChannel.read(buffer);
                if (bytesRead > 0) {
                    buffer.flip();
                    byte[] data = new byte[bytesRead];
                    buffer.get(data);
                    String receivedMessage = new String(data);
                    System.out.println("Reader thread read: " + receivedMessage);
                }
            } catch (IOException e) {
                log.error("read error:", e);
            }
        });

        // 启动线程
        writerThread.start();
        readerThread.start();

        // 等待线程完成
        writerThread.join();
        readerThread.join();
    }
}