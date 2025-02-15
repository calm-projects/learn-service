package com.learn.java.jvm.chapter12;

/**
 * volatile使用场景
 */
public class VolatileTest2 {
    volatile boolean shutdownRequested;
    public void shutdown() {
        shutdownRequested = true;
    }
    public void doWork() {
        while (!shutdownRequested) {
            // 代码的业务逻辑
        }
    }
}