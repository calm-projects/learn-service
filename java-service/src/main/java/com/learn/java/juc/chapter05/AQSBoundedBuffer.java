package com.learn.java.juc.chapter05;

import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class AQSBoundedBuffer<E> {
    final Lock lock = new ReentrantLock();
    final Condition notFull;
    final Condition notEmpty;

    final Object[] items;
    int putIndex, takeIndex, count;

    public AQSBoundedBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        items = new Object[capacity];
        notFull = lock.newCondition();
        notEmpty = lock.newCondition();
    }

    public static void main(String[] args) {
        AQSBoundedBuffer<Integer> boundedBuffer = new AQSBoundedBuffer<>(5);
        System.out.println("Initial buffer: " + Arrays.toString(boundedBuffer.items));

        new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    boundedBuffer.put(i);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                for (int i = 0; i < 12; i++) {
                    boundedBuffer.take();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void put(E x) throws InterruptedException {
        lock.lock();
        try {
            // Wait if the buffer is full
            while (count == items.length)
                notFull.await();

            items[putIndex] = x;
            if (++putIndex == items.length) putIndex = 0;
            count++;
            notEmpty.signalAll();  // Signal all threads waiting on notEmpty
            System.out.println("put----> " + x);
        } finally {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        lock.lock();
        try {
            // Wait if the buffer is empty
            while (count == 0)
                notEmpty.await();

            E x = (E) items[takeIndex];
            if (++takeIndex == items.length) takeIndex = 0;
            count--;
            notFull.signalAll();  // Signal all threads waiting on notFull
            System.out.println("take----> " + x);
            return x;
        } finally {
            lock.unlock();
        }
    }
}
