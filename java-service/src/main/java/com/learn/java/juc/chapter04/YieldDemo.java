package com.learn.java.juc.chapter04;

public class YieldDemo {

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 500; i++) {
                System.out.println("Thread T1: " + i);
                if (i == 2) {
                    System.out.println("T1 yielding CPU...");
                    Thread.yield(); // 建议让出 CPU
                }
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 500; i++) {
                System.out.println("Thread T2: " + i);
            }
        });

        t1.start();
        t2.start();
    }
}