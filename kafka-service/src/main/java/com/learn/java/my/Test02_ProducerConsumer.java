package com.learn.java.my;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Test02_ProducerConsumer {
    /**
     * 消费信息：
     * kafka-console-consumer.sh --bootstrap-server hadoop02:9092 --topic test --from-beginning
     * 查看偏移量信息:
     * kafka-run-class.sh kafka.tools.GetOffsetShell --topic test --bootstrap-server hadoop02:9092
     */
    @Test
    public void testProducer() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop02:9092,hadoop03:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(config)) {
            for (int i = 0; i < 10; i++) {
                ProducerRecord<String, String> record = new ProducerRecord<>(
                        "test", null, "tom-" + i
                );
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        log.error("producer error:", exception);
                    }
                });
            }
        }
    }


    @Test
    public void testConsumer() {

    }
}
