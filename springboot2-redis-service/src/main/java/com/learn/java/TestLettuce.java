package com.learn.java;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Test;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;

import java.util.HashMap;
import java.util.Map;

/**
 * 报错：Caused by: java.lang.ClassNotFoundException: reactor.core.publisher.Sinks
 * 版本冲突
 */
public class TestLettuce {
    @Test
    void test_connection() {
        RedisURI uri = RedisURI.Builder
                .redis("localhost", 6379)
                .build();

        RedisClient client = RedisClient.create(uri);
        StatefulRedisConnection<String, String> connection = client.connect();
        RedisCommands<String, String> commands = connection.sync();

        commands.set("foo", "bar");
        String result = commands.get("foo");
        System.out.println(result); // >>> bar

        connection.close();

        client.shutdown();
    }


    @Test
    void test_cluster_connection() {
        RedisURI redisURI = RedisURI.Builder
                .redis("localhost", 6379)
                .build();
        try (RedisClusterClient clusterClient = RedisClusterClient.create(redisURI)) {
            StatefulRedisClusterConnection<String, String> connection = clusterClient.connect();

            //...

            connection.close();
        }
    }

    @Test
    void test_async() {
        RedisClient redisClient = RedisClient.create("redis://localhost:6379");

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisReactiveCommands<String, String> reactiveCommands = connection.reactive();

            // Reactively store & retrieve a simple string
            reactiveCommands.set("foo", "bar").block();
            reactiveCommands.get("foo").doOnNext(System.out::println).block(); // prints bar

            // Reactively store key-value pairs in a hash directly
            Map<String, String> hash = new HashMap<>();
            hash.put("name", "John");
            hash.put("surname", "Smith");
            hash.put("company", "Redis");
            hash.put("age", "29");
            reactiveCommands.hset("user-session:124", hash)
                            .then(reactiveCommands.hgetall("user-session:124")
                                    .collectMap(KeyValue::getKey, KeyValue::getValue)
                                    .doOnNext(System.out::println)).block();
            // Prints: {surname=Smith, name=John, company=Redis, age=29}
        } finally {
            redisClient.shutdown();
        }
    }
}
