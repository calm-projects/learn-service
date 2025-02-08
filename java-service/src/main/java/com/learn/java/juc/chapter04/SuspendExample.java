package com.learn.java.juc.chapter04;

public class SuspendExample {
    private static final Object lock = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("Thread 1 acquired the lock");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Thread.currentThread().suspend();  // 停止线程执行，锁没有释放
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (lock) {  // Thread 2 试图获取锁，死锁发生
                System.out.println("Thread 2 acquired the lock");
            }
        });

        t1.start();
        t2.start();
    }
}

