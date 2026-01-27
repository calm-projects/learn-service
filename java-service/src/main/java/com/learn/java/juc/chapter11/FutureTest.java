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

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String result = "complete";
        Future<?> future = executor.submit(() -> {
            while(true){
                System.out.println(111);
            }
        }, result);
        System.out.println(future.isDone());
        TimeUnit.SECONDS.sleep(1);
        System.out.println(future.cancel(true));
        System.out.println(future.isCancelled());
        System.out.println(future.isDone());
    }

    @Test
    public void testCancel() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String result = "complete";
        Future<?> future = executor.submit(() -> {
            while(true){
                System.out.println(111);
            }
        }, result);
        System.out.println(future.isDone());
        TimeUnit.SECONDS.sleep(1);
        System.out.println(future.cancel(true));
        System.out.println(future.isCancelled());
        System.out.println(future.isDone());
    }
}
