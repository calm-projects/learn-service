package com.learn.java.juc.chapter11;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class FutureTest {
    @Data(staticConstructor = "of")
    static class User {
        String name;
    }

    @Test
    public void testCancel() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // 直接就传递一个变量
        String result = "complete";
        AtomicReference<String> atomicReference = new AtomicReference<>("init value");
        User user = User.of();
        Future<?> future = executor.submit(() -> {
            try {
                System.out.println("start ...");
                TimeUnit.SECONDS.sleep(5);
                atomicReference.set("update value");
                user.setName("tom");
                System.out.println("end ...");
            } catch (InterruptedException e) {
                log.error("interrupted -> ", e);
                Thread.currentThread().interrupt(); // 恢复中断状态
                /*
                    下面我调用了cancel，cancel底层是interrupt，interrupt遇到sleep会报中断异常，我打印了日志信息然后抛出了一个运行时异常
                    但是程序没有运行失败为什么？看下Future的run方法是如何调用的
                    try {
                        result = c.call();
                        ran = true;
                    } catch (Throwable ex) {
                        result = null;
                        ran = false;
                        setException(ex);
                    }
                    这里直接捕获了异常，然后设置了setException，所以程序不会直接报错
                 */
                throw new RuntimeException(e);
            }
        }, result);
        /*
            result:complete ; atomic:update value; user:tom, 这里只是简单说明下 Runnable也是可以获取返回值的，
            Runnable不是lambda而是一个实现类，run方法拿不到引用？ 也可以给实现类创建一个类变量，通过构造方法传递进去，run便可以操作类变量。
            也可以直接使用Callable 本身就自带返回值。
            每一种实现都可以，关键是看编写代码的时候业务逻辑如何。
            todo 这里我们主要测试cancel，需要把下面代码注释掉
         */
        // log.info("result:{}; atomic:{}; user:{}", future.get(), atomicReference.get(), user.getName());

        // false
        System.out.println(future.isDone());
        TimeUnit.SECONDS.sleep(1);
        // true， 这里不推荐使用cancel判断任务是否取消，不准确
        System.out.println(future.cancel(true));
        // true
        System.out.println(future.isCancelled());
        // true
        System.out.println(future.isDone());
    }
}
