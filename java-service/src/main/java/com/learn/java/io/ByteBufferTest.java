package com.learn.java.io;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

public class ByteBufferTest {

    @Test
    public void test_getInt() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 1);
        byteBuffer.put((byte) 1);
        byteBuffer.flip();
        // SCOPED_MEMORY_ACCESS.getIntUnaligned(scope(), hb, byteOffset(nextGetIndex(4)), bigEndian);
        // nextGetIndex(4)，四位一体，这里输出为 0
        System.out.println(byteBuffer.getInt());
        // 257
        System.out.println(byteBuffer.getInt());
        // [pos=8 lim=8 cap=8]
        System.out.println(byteBuffer);
    }


    @Test
    public void test_toLongBuffer() {
        // LITTLE_ENDIAN, 本地是小端字节顺序，但是java 默认按照大端存储
        System.out.println(ByteOrder.nativeOrder());
        // 为什么设定8字节，因为Long类型最低是8字节，一会转换的时候要 >> 3位，看下源码就知道了
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 0);
        byteBuffer.put((byte) 1);
        byteBuffer.put((byte) 1);
        // 这里必须调用flip，因为>>3的时候是调用的剩余元素右移
        byteBuffer.flip();
        LongBuffer longBuffer = byteBuffer.asLongBuffer();
        // java.nio.HeapByteBuffer[pos=0 lim=8 cap=8]
        System.out.println(byteBuffer);
        // java.nio.ByteBufferAsLongBufferB[pos=0 lim=1 cap=1]
        // 这里将字节容量压缩为了Long容量8个字节是一个Long
        System.out.println(longBuffer);
        // 257，大端字节顺序(在获取的时候会计算处理，我们直接看代码的时候就正常看即可)，也就是 0001 0000 0001 这个是257
        System.out.println(longBuffer.get());
    }

}
