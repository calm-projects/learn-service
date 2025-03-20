package com.learn.java.juc.chapter11;

import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ExecuteServiceTest {
    @Test
    public void testShutdown() throws Exception {
        /*
            true
            false
            开始执行...
            结束执行...
            说明调用了shutdown之后正在执行的任务还是运行的
         */
        ExecutorService executorService = runTask(5);
        executorService.shutdown();
        System.out.println(executorService.isShutdown());
        System.out.println(executorService.isTerminated());
        TimeUnit.SECONDS.sleep(10);
    }

    private ExecutorService runTask(int sleep) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        // 如果是execute则直接抛出异常，如果是submit不会抛出异常，submit 是FutureTask，会对run抛出的异常捕获
        executorService.execute(() -> {
            try {
                System.out.println("开始执行...");
                TimeUnit.SECONDS.sleep(sleep);
                System.out.println("结束执行...");
            } catch (InterruptedException e) {
                System.out.println("中断异常" + e);
                // throw new RuntimeException("sleep中断异常....");
            }
        });
        return executorService;
    }

    @Test
    public void testShutdownNow() throws Exception {
        /*
            true
            false
            开始执行...
            调用shutdownNow会试图interrupted线程，而我的线程处于sleep状态,固抛出异常java.lang.InterruptedException: sleep interrupted
         */
        ExecutorService executorService = runTask(5);
        executorService.shutdownNow();
        System.out.println(executorService.isShutdown());
        System.out.println(executorService.isTerminated());
        TimeUnit.SECONDS.sleep(10);
    }


    @Test
    public void testAwaitTermination() throws Exception {
        ExecutorService executorService = runTask(5);
        try {
            executorService.shutdown();
            System.out.println(executorService.isShutdown());
            System.out.println(executorService.isTerminated());
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                // 等待中断超时，尝试interrupt中断运行的任务，run可能会抛出InterruptException
                List<Runnable> tasks = executorService.shutdownNow();
                System.out.println("first shutdownNow 遗留 task:" + tasks.size());
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.out.println("executorService 中断超时");
                }
            }
        } catch (InterruptedException e) {
            // 主要是防止当前线程被中断，无法shutdown执行器，shutdownNow后保持当前线程的中断状态.
            // (Re-) Cancel if current thread also interrupted
            List<Runnable> tasks = executorService.shutdownNow();
            System.out.println("second shutdownNow 遗留 task:" + tasks.size());
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testSubmit() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> submit = executorService.submit(() -> {
            try {
                System.out.println("开始执行...");
                TimeUnit.SECONDS.sleep(1);
                System.out.println("结束执行...");
            } catch (InterruptedException e) {
                System.out.println("中断异常" + e);
            }
        });
        Object object = submit.get();
        // null
        System.out.println(object);
        // 这里写法可能存在很多种，但是无非就是引用传递
        Result result = Result.of();
        Future<Result> future = executorService.submit(() -> {
            try {
                System.out.println("开始执行...");
                TimeUnit.SECONDS.sleep(1);
                result.setName("tom");
                System.out.println("结束执行...");
            } catch (InterruptedException e) {
                System.out.println("中断异常" + e);
            }
        }, result);
        // tom
        System.out.println(result.getName());
        String name = future.get().getName();
        // tom
        System.out.println(name);

    }

    @Data(staticConstructor = "of")
    static class Result{
        String name;
    }

}
