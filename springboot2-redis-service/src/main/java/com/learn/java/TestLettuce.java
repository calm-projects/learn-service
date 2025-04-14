package com.learn.java;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Test;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;

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
}
