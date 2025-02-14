package com.learn.java.juc.chapter05;

public class ReentrantTest{
    private static final Object lock = new Object();
    public static void main(String[] args) {
        synchronized (lock){
            synchronized (lock){
                System.out.println(1);
            }
        }
    }
}
