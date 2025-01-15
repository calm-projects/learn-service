package com.learn.java.jvm.chapter2;

/**
 * 通过定义多个线程，每个线程会消耗一个stack frame内存，当内存耗尽时，也会内存溢出。
 * Java的线程是映射到操作系统的内核线程上，无限制地创建线程会对操作系统带来很大压力，上述代码执行时有很高的风险，
 * 可能会由于创建线程数量过多而导致操作系统假死。
 */
public class JavaVMStackSOF3 {
    private void dontStop() {
        while (true) {
        }
    }
    public void stackLeakByThread() {
        while (true) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    dontStop();
                }
            });
            thread.start();
        }
    }
    public static void main(String[] args) throws Throwable {
        JavaVMStackSOF3 oom = new JavaVMStackSOF3();
        oom.stackLeakByThread();
    }
}