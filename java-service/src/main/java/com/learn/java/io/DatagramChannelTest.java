package com.learn.java.io;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DatagramChannelTest {

    private static final String HOST = "localhost";
    private static final int PORT = 8888;


    @Test
    public void write() {
        try (DatagramChannel channel = DatagramChannel.open().connect(new InetSocketAddress(HOST, PORT))) {
            channel.configureBlocking(false);
            for (int i = 0; i < 10; i++) {
                String packages = "send package: %s".formatted(i);
                channel.write(ByteBuffer.wrap(packages.getBytes(StandardCharsets.UTF_8)));
                TimeUnit.SECONDS.sleep(1);
                log.info(packages);
            }
        } catch (InterruptedException | IOException e) {
            log.error("error:", e);
        }
    }


    @Test
    public void read() {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(PORT));
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            while (true) {
                byteBuffer.clear();
                SocketAddress socketAddress = channel.receive(byteBuffer);
                byteBuffer.flip();
                log.info("ip:{}, package:{}", socketAddress.toString(),
                        new String(byteBuffer.array(), 0, byteBuffer.limit(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            log.error("error:", e);
        }
    }
}