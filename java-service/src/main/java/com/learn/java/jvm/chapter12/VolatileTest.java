package com.learn.java.jvm.chapter12;

/**
 * volatile变量自增运算测试
 */
public class VolatileTest {
    public static volatile int race = 0;
    public static void increase() {
        race++;
    }
    private static final int THREADS_COUNT = 5;
    public static void main(String[] args) {
        Thread[] threads = new Thread[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        increase();
                    }
                }
            });
            threads[i].start();
        }

        // 使用 join 等待所有线程完成
        for (int i = 0; i < THREADS_COUNT; i++) {
            try {
                threads[i].join();  // 等待每个子线程完成
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(race);
    }
}