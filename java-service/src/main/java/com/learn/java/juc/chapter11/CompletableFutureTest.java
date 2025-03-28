package com.learn.java.juc.chapter11;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CompletableFutureTest {

    @Test
    public void test1() {
        System.out.println(System.currentTimeMillis());
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("execute first state....");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).thenRunAsync(() -> {
            System.out.println("execute second state....");
        }).whenCompleteAsync((unused, throwable) -> System.out.println("execute end ...."));
        future.join();
        System.out.println(System.currentTimeMillis());
    }
}
