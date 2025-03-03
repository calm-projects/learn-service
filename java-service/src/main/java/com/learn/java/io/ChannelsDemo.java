package com.learn.java.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ChannelsDemo {

    public static void main(String[] args) throws IOException {
        // Step 1: Create input stream and convert it to a ReadableByteChannel
        try (FileInputStream inputStream = new FileInputStream("java-service/src/main/java/com/learn/java/io/ChannelsDemo.java")) {
            ReadableByteChannel readableChannel = Channels.newChannel(inputStream);

            // Step 2: Create output stream and convert it to a WritableByteChannel
            try (FileOutputStream outputStream = new FileOutputStream("java-service/src/main/java/com/learn/java/io/ChannelsDemo.txt")) {
                WritableByteChannel writableChannel = Channels.newChannel(outputStream);

                // Step 3: Transfer data from readableChannel to writableChannel
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (readableChannel.read(buffer) != -1) {
                    buffer.flip(); // Prepare buffer for writing
                    writableChannel.write(buffer); // Write data to the output channel
                    buffer.clear(); // Clear buffer for next read
                }

                System.out.println("Data transferred successfully.");
            }
        }
    }
}