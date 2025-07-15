package com.learn.java.juc.chapter04;

import java.util.concurrent.TimeUnit;

public class ThreadLocalTest {

    // 创建一个ThreadLocal变量，用于存储用户ID
    private static final ThreadLocal<String> userThreadLocal = ThreadLocal.withInitial(() -> "DefaultUser");

    public static void main(String[] args) throws InterruptedException {
        // 创建两个线程模拟同时处理不同用户的请求
        Thread thread1 = new Thread(new Task(), "Thread-1");
        Thread thread2 = new Thread(new Task(), "Thread-2");

        thread1.start();
        thread2.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println(Thread.currentThread().getName() + userThreadLocal.get());
    }

    static class Task implements Runnable {
        @Override
        public void run() {
            // 设置当前线程的用户ID
            if(Thread.currentThread().getName().equals("Thread-1")) {
                userThreadLocal.set("User-1");
            } else {
                userThreadLocal.set("User-2");
            }

            System.out.println(Thread.currentThread().getName() + " handling request for: " + userThreadLocal.get());

            // 模拟一些操作后再次检查用户ID
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally{
                // 线程经常被复用，尤其是在线程池的情况下，如果不回收会造成内存泄漏，
                // 以及可能会影响后续的其他流程。
                userThreadLocal.remove();
            }
            System.out.println(Thread.currentThread().getName() + " finished handling request for: " + userThreadLocal.get());
        }
    }
}
