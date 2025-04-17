package com.learn.java;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.CountDownLatch;

@SpringBootTest
@Slf4j
public class TestRedisson {

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private int count = 0;

    @Test
    public void test() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(200);
        for (int i = 0; i < 200; i++) {
            new Thread(() -> {
                RLock lock = redissonClient.getLock("counter:lock");
                try {
                    lock.lock();
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
    void test_stringRedisTemplate() {
        stringRedisTemplate.opsForValue().set("name", "tom");
        System.out.println(stringRedisTemplate.opsForValue().get("name"));
    }
}
