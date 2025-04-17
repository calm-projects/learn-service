//package com.learn.java;
//
//import org.junit.jupiter.api.Test;
//import redis.clients.jedis.*;
//
//import java.time.Duration;
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//public class TestJedis {
//    /**
//     * 测试常规连接
//     */
//    @Test
//    void test_connection() {
//        UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6379");
//        // Jedis jedis = new Jedis("redis://localhost:6379");
//        String res1 = jedis.set("bike:1", "Deimos");
//        System.out.println(res1); // OK
//        String res2 = jedis.get("bike:1");
//        System.out.println(res2); // Deimos
//        jedis.close();
//    }
//
//    @Test
//    void test_cluster_connection() {
//        Set<HostAndPort> jedisClusterNodes = Stream.of(
//                        new HostAndPort("host1", 6379),
//                        new HostAndPort("host2", 6379))
//                .collect(Collectors.toSet());
//        JedisCluster jedis = new JedisCluster(jedisClusterNodes);
//        String res1 = jedis.set("bike:1", "Deimos");
//        System.out.println(res1); // OK
//        String res2 = jedis.get("bike:1");
//        System.out.println(res2); // Deimos
//        jedis.close();
//    }
//
//    @Test
//    void test_connection_pool(){
//        JedisPool pool = new JedisPool("localhost", 6379);
//        try (Jedis jedis = pool.getResource()) {
//            // Store & Retrieve a simple string
//            jedis.set("foo", "bar");
//            System.out.println(jedis.get("foo")); // prints bar
//
//            // Store & Retrieve a HashMap
//            Map<String, String> hash = new HashMap<>();;
//            hash.put("name", "John");
//            hash.put("surname", "Smith");
//            hash.put("company", "Redis");
//            hash.put("age", "29");
//            jedis.hset("user-session:123", hash);
//            System.out.println(jedis.hgetAll("user-session:123"));
//            // Prints: {name=John, surname=Smith, company=Redis, age=29}
//        }
//    }
//
//    /**
//     * JedisPooled是在JedisPool 4.0.0版本中添加的，它提供了类似于JedisPool的功能，但具有更直接的API。
//     */
//    @Test
//    void test_connection_pooled(){
//        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
//        // maximum active connections in the pool,
//        // tune this according to your needs and application type
//        // default is 8
//        poolConfig.setMaxTotal(8);
//        // maximum idle connections in the pool, default is 8
//        poolConfig.setMaxIdle(8);
//        // minimum idle connections in the pool, default 0
//        poolConfig.setMinIdle(0);
//        // Enables waiting for a connection to become available.
//        poolConfig.setBlockWhenExhausted(true);
//        // The maximum number of seconds to wait for a connection to become available
//        poolConfig.setMaxWait(Duration.ofSeconds(1));
//        // Enables sending a PING command periodically while the connection is idle.
//        poolConfig.setTestWhileIdle(true);
//        // controls the period between checks for idle connections in the pool
//        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(1));
//
//        JedisPooled jedis = new JedisPooled(poolConfig, "localhost", 6379);
//        // Store & Retrieve a simple string
//        jedis.set("foo", "bar1");
//        System.out.println(jedis.get("foo")); // prints bar1
//
//        // Store & Retrieve a HashMap
//        Map<String, String> hash = new HashMap<>();;
//        hash.put("name", "John1");
//        hash.put("surname", "Smith1");
//        hash.put("company", "Redis1");
//        hash.put("age", "29");
//        jedis.hset("user-session:123", hash);
//        System.out.println(jedis.hgetAll("user-session:123"));
//        // Prints: {name=John1, surname=Smith1, company=Redis1, age=29}
//    }
//}
