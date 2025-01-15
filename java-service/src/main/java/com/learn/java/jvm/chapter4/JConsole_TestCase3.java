package com.learn.java.jvm.chapter4;

/**
 * -Xms100m -Xmx100m -XX:+UseSerialGC
 */
public class JConsole_TestCase3 {
    /**
     * 线程死锁等待演示
     */
    static class SynAddRunalbe implements Runnable {
        int a, b;

        public SynAddRunalbe(int a, int b) {
            this.a = a;
            this.b = b;
        }

        /**
         * Integer.valueOf()方法出于减少对象创建次数和节省内存的考虑，会对数值为-128～127之间的
         * Integer对象进行缓存，如果valueOf()方法传入的参数在这个范围之内，就直接返回缓存中的对象。
         */
        @Override
        public void run() {
            synchronized (Integer.valueOf(a)) {
                synchronized (Integer.valueOf(b)) {
                    System.out.println(a + b);
                }
            }
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new Thread(new SynAddRunalbe(1, 2)).start();
            new Thread(new SynAddRunalbe(2, 1)).start();
        }
    }
}
