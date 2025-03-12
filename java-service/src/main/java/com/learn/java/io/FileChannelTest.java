package com.learn.java.io;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileChannelTest {

    @Test
    public void test_FileChannel() {
        String src = "src/main/java/com/learn/java/io/ChannelTest.java";
        String dst = "src/main/java/com/learn/java/io/ChannelTest.txt";
        try (FileInputStream inputStream = new FileInputStream(src);
             FileOutputStream outputStream = new FileOutputStream(dst);
             FileChannel srcChannel = inputStream.getChannel();
             FileChannel dstChannel = outputStream.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(10);
            while (srcChannel.read(buffer) > 0) {
                // 打印到控制台
                buffer.flip();
                while (buffer.hasRemaining()) {
                    System.out.print((char) buffer.get());
                }
                // 输出到文件，可以使用 transferTo 或者 transferFrom
                buffer.flip();
                dstChannel.write(buffer);
                // 清空buffer
                buffer.clear();
            }
        } catch (IOException e) {
            log.error("文件读取失败: ", e);
        }
    }


    @Test
    public void test_ScatterAndGather() {
        // Gather
        Path path = Paths.get("src/main/java/com/learn/java/io/ChannelTest.txt");
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteBuffer[] buffers = new ByteBuffer[3];
            // encode 默认Position 为 0
            buffers[0] = StandardCharsets.UTF_8.encode("你好");
            buffers[1] = StandardCharsets.UTF_8.encode("我好");
            buffers[2] = StandardCharsets.UTF_8.encode("大家好");
            long write = channel.write(buffers);
            log.info("写入的字节数:{}", write);
        } catch (IOException e) {
            log.error("test_ScatterAndGather error:", e);
        }

        // Scatter
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer[] buffers = new ByteBuffer[3];
            buffers[0] = ByteBuffer.allocate(3);
            buffers[1] = ByteBuffer.allocate(6);
            buffers[2] = ByteBuffer.allocate(12);
            long read = channel.read(buffers);
            log.info("读取的字节数:{}", read);
            for (ByteBuffer buffer : buffers) {
                buffer.flip();
                String content = StandardCharsets.UTF_8.decode(buffer).toString();
                System.out.println(content);
            }
        } catch (IOException e) {
            log.error("test_ScatterAndGather error:", e);
        }
    }

    @Test
    public void test_transferTo() {
        String from = "src/main/java/com/learn/java/io/ChannelTest.java";
        String to = "src/main/java/com/learn/java/io/ChannelTest.txt";
        try (FileInputStream fromFile = new FileInputStream(from);
             FileOutputStream toFile = new FileOutputStream(to)) {
            FileChannel fromChannel = fromFile.getChannel();
            FileChannel toChannel = toFile.getChannel();
            // 零拷贝（Zero-Copy）,下面俩个作用是一样的
            // toChannel.transferFrom(fromChannel, 0, fromChannel.size());
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } catch (IOException e) {
            log.error("test_transferTo error:", e);
        }
    }
}
