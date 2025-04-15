package com.learn.java.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisController {
    // 这里引入必须和RedisConfig中的泛型保持一致
    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/set")
    public String set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
        return String.valueOf(redisTemplate.opsForValue().get(key));
    }
}
