package com.learn.java.juc.chapter04;

import java.util.concurrent.TimeUnit;

/**
 * 6-4
 */
public class SleepUtils {
    public static final void second(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().isInterrupted());
            System.out.println(1111);
        }
    }
}
