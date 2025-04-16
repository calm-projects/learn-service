package com.learn.java.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash
@Data
public class User {
    @Id private String id;
    private String username;
    private String password;
}
