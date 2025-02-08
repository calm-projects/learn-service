package com.learn.java.juc.chapter04;

import java.util.concurrent.TimeUnit;

public class StopExample {
    // 模拟一些资源，比如当flag为true的时候，config资源才配置完成
    private static boolean flag = false;
    private static int config = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            // 验证下stop会释放锁
            synchronized (StopExample.class) {
                flag = true;
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                config = 1;
            }
            System.out.println("此处代码未执行");
        });
        t1.start();

        // 强制停止线程
        try {
            TimeUnit.MILLISECONDS.sleep(1);
            t1.stop();
        } catch (ThreadDeath td) {
        }

        // 结果发现flag为true了，但是config并没有配置完成，当其他线程根据flag获取config的时候，就会获取到错误的config
        synchronized (StopExample.class) {
            System.out.println(flag);
            System.out.println(config);
        }
    }
}

