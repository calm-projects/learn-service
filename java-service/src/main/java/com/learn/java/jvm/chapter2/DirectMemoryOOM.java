package com.learn.java.jvm.chapter2;

import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;


/**
 * VM Args：-Xms20M -Xmx20M -XX:MaxDirectMemorySize=10M
 * 直接内存（Direct Memory）的容量大小可通过-XX：MaxDirectMemorySize参数来指定，如果不去指定，则默认与Java堆最大值（由-Xmx指定）一致。
 * 使用DirectByteBuffer分配内存也会抛出内存溢出异常，但它抛出异常时并没有真正向操作系统申请分配内存，而是通过计算得知内存无法分配就会
 * 在代码里手动抛出溢出异常，真正申请分配内存的方法是Unsafe::allocateMemory()。
 * TODO 此代码设置的MaxDirectMemorySize未生效，直接将电脑卡死了，所以添加了一个while num循环，接着往后看文档自然就知道了
 */
public class DirectMemoryOOM {
    private static final int _1MB = 1024 * 1024;

    @Test
    public void test_direct_allocate() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(50 * _1MB);
        System.out.println(byteBuffer);
    }

    @Test
    public void test_unsafe_allocate() throws IllegalAccessException {
        Field unsafeField = Unsafe.class.getDeclaredFields()[0];
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        int num = 0;
        while (num < 50) {
            System.out.println(num++);
            try {
                unsafe.allocateMemory(_1MB);
            } catch (OutOfMemoryError e) {
                System.out.println("Failed to allocate memory: " + e.getMessage());
            }
        }
    }
}
