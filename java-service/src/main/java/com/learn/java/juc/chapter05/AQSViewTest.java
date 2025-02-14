package com.learn.java.juc.chapter05;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AQSViewTest {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        lock.lock();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}

class AQSViewTest2 {
    // 创建一个ReentrantReadWriteLock实例
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static int sharedData = 0;  // 共享数据

    public static void main(String[] args) throws InterruptedException {
        new Thread(AQSViewTest2::readData).start();
        new Thread(AQSViewTest2::readData).start();
        new Thread(() -> writeData(1)).start();
        new Thread(() -> writeData(2)).start();
    }

    public static void readData() {
        lock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " start 读取数据: " + sharedData);
            TimeUnit.SECONDS.sleep(3);
            System.out.println(Thread.currentThread().getName() + " end 读取数据: " + sharedData);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void writeData(int newValue) {
        lock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " start 写入数据: " + sharedData);
            TimeUnit.SECONDS.sleep(3);
            sharedData = newValue;
            System.out.println(Thread.currentThread().getName() + " end 写入数据: " + sharedData);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}


class AQSViewTest3 {

    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        // 启动多个线程进行锁的尝试
        for (int i = 0; i < 2; i++) {
            Thread thread = new Thread(AQSViewTest3::tryAcquireLock, "Thread-" + i);
            thread.start();
            if (i == 0){
                TimeUnit.SECONDS.sleep(2);
                thread.interrupt();
            }
        }
    }

    private static void tryAcquireLock() {
        boolean acquired = false;
        try {
            acquired = lock.tryLock(1, TimeUnit.SECONDS);
            if (acquired) {
                System.out.println(Thread.currentThread().getName() + " acquired the lock.。。。。。。。1");
                TimeUnit.SECONDS.sleep(5);
                System.out.println(Thread.currentThread().getName() + " acquired the lock.。。。。。。。2");
            } else {
                System.out.println(Thread.currentThread().getName() + " failed to acquire the lock within the timeout.");
            }
        } catch (InterruptedException e) {
            handleInterruptedException(e);
        } finally {
            if (acquired) {
                lock.unlock();
                System.out.println(Thread.currentThread().getName() + " released the lock.");
            }
        }
    }

    private static void handleInterruptedException(InterruptedException e) {
        // 会输出false 中断sleep不会设置中断标识
        System.out.println(Thread.currentThread().getName() + "->中断标识：" + Thread.currentThread().isInterrupted());
        Thread.currentThread().interrupt();
        // 再次中断会变为true
        System.out.println(Thread.currentThread().getName() + "->中断标识：" + Thread.currentThread().isInterrupted());
    }
}

class CachedData {
    static volatile int data;
    static volatile boolean cacheValid;
    static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public static void main(String[] args) {
        processCachedData();
    }

    static void processCachedData() {
        rwl.readLock().lock();
        if (!cacheValid) {
            // Must release read lock before acquiring write lock
            rwl.readLock().unlock();
            rwl.writeLock().lock();
            try {
                // Recheck state because another thread might have
                // acquired write lock and changed state before we did.
                if (!cacheValid) {
                    data = 1;
                    cacheValid = true;
                }
                // Downgrade by acquiring read lock before releasing write lock
                rwl.readLock().lock();
            } finally {
                rwl.writeLock().unlock(); // Unlock write, still hold read
            }
        }

        try {
            System.out.println(data);
        } finally {
            rwl.readLock().unlock();
        }
    }
}

