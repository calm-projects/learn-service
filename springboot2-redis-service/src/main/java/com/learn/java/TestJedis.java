package com.learn.java;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestJedis {
    /**
     * 测试常规连接
     */
    @Test
    void test_connection() {
        UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6379");
        // Jedis jedis = new Jedis("redis://localhost:6379");
        String res1 = jedis.set("bike:1", "Deimos");
        System.out.println(res1); // OK
        String res2 = jedis.get("bike:1");
        System.out.println(res2); // Deimos
        jedis.close();
    }

    @Test
    void test_cluster_connection() {
        Set<HostAndPort> jedisClusterNodes = Stream.of(
                        new HostAndPort("host1", 6379),
                        new HostAndPort("host2", 6379))
                .collect(Collectors.toSet());
        JedisCluster jedis = new JedisCluster(jedisClusterNodes);
        String res1 = jedis.set("bike:1", "Deimos");
        System.out.println(res1); // OK
        String res2 = jedis.get("bike:1");
        System.out.println(res2); // Deimos
        jedis.close();
    }

    @Test
    void test_connection_pool(){
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            // Store & Retrieve a simple string
            jedis.set("foo", "bar");
            System.out.println(jedis.get("foo")); // prints bar

            // Store & Retrieve a HashMap
            Map<String, String> hash = new HashMap<>();;
            hash.put("name", "John");
            hash.put("surname", "Smith");
            hash.put("company", "Redis");
            hash.put("age", "29");
            jedis.hset("user-session:123", hash);
            System.out.println(jedis.hgetAll("user-session:123"));
            // Prints: {name=John, surname=Smith, company=Redis, age=29}
        }
    }
}
