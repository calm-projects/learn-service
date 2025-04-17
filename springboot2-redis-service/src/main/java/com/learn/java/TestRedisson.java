package com.learn.java;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class TestRedisson {

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private int count = 0;

    @Test
    public void test_lock() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(200);
        for (int i = 0; i < 200; i++) {
            new Thread(() -> {
                RLock lock = redissonClient.getLock("counter:lock");
                try {
                    /*
                        看下底层源码续期，leaseTime >0 则不使用schedule
                        if (leaseTime > 0) {
                            internalLockLeaseTime = unit.toMillis(leaseTime);
                        } else {
                            scheduleExpirationRenewal(threadId);
                        }
                     */
                    // 等待获取锁，默认的过期时间为30*1000ms，watchdog会自动续期。
                    lock.lock();
                    // 如果在给定的等待时间内有空闲，并且当前线程尚未 中断，则获取该锁，默认的过期时间为30*1000ms，watchdog会自动续期。
                    // boolean flag = lock.tryLock(30, TimeUnit.SECONDS);
                    // 等待获取锁，获取锁后，锁10s过期，没有watchdog给过期时间续期
                    // lock.lock(10, TimeUnit.SECONDS);
                    this.count = this.count + 1;
                } finally {
                    lock.unlock();
                }
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        log.info("count:{}", this.count);
    }

    @Test
    public void test_tryLock() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(200);
        for (int i = 0; i < 200; i++) {
            new Thread(() -> {
                RLock lock = redissonClient.getLock("counter:lock");
                try {
                    boolean flag = lock.tryLock(30, TimeUnit.SECONDS);
                    if (flag) {
                        try {
                            this.count = this.count + 1;
                        } finally {
                            lock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        log.info("count:{}", this.count);
    }

    @Test
    void test_stringRedisTemplate() {
        stringRedisTemplate.opsForValue().set("name", "tom");
        System.out.println(stringRedisTemplate.opsForValue().get("name"));
    }
}
