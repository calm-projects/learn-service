package com.learn.java.io;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class ChannelTest {

    @Test
    public void test_FileChannel() {
        String src = "src/main/java/Main.java";
        String dst = "src/main/java/MainOutPut.txt";
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
}
