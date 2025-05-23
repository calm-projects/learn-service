package com.learn.java.my;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;

@Slf4j
public class Test01_Admin {

    /**
     * 这里如果不知道使用哪些类，就去看看源代码有哪些类，比如根据KafkaAdminClient可以看到同目录下的AdminClientConfig
     * 验证： kafka-topics.sh --bootstrap-server hadoop02:9092 --list
     */
    @Test
    public void testCreateTopic() {
        HashMap<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop02:9092,hadoop03:9092");
        try (AdminClient adminClient = AdminClient.create(config)) {
            NewTopic topic = new NewTopic("t1", 3, (short) 2);
            adminClient.createTopics(Collections.singleton(topic));
        }
    }
}
