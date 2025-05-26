package com.learn.java.my;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.singleton;

@Slf4j
public class Test02_ProducerConsumer {
    /**
     * 消费信息：
     * kafka-console-consumer.sh --bootstrap-server hadoop02:9092 --topic test --from-beginning
     * 查看偏移量信息:
     * kafka-run-class.sh kafka.tools.GetOffsetShell --topic test --bootstrap-server hadoop02:9092
     * 查看log文件
     * kafka-run-class.sh kafka.tools.DumpLogSegments --files /opt/service/kafka/kafka-logs/test-1/00000000000000000000.log --max-message-size 10 --print-data-log
     */
    @Test
    public void testProducer() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop02:9092,hadoop03:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        // 里面还有好多其他配置，比如幂等性、batch.size、buffer.memory等等
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(config)) {
            for (int i = 0; i < 10; i++) {
                ProducerRecord<String, String> record = new ProducerRecord<>(
                        "test", null, "tom-" + i
                );
                // 同步发送
                producer.send(record).get();
                // 同步带回调发送
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        log.error("producer error:", exception);
                    }
                }).get();
                // 异步发送
                producer.send(record);
                // 异步带回调发送
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        log.error("producer error:", exception);
                    }
                });
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查看消费者组列表
     * kafka-consumer-groups.sh --bootstrap-server hadoop03:9092 --list
     * 查看消费者组消费情况， CURRENT-OFFSET消费者偏移量，LOG-END-OFFSET LEO分区最大偏移量 LAG 未消费的偏移量，消费的时候不是根据LEO，而是根据HW消费。概念不理解的可以看下文档。
     * 重启Consumer后，消费者会根据偏移量进行消费
     * [hadoop@hadoop03 ~]$ kafka-consumer-groups.sh --bootstrap-server hadoop03:9092 --group group_test01 --describe
     * <p>
     * GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                                  HOST            CLIENT-ID
     * group_test01    test            0          3               3               0               consumer-group_test01-1-ef8979ba-4696-49b2-b26c-949deb2fd576 /192.168.3.214  consumer-group_test01-1
     * group_test01    test            1          10              10              0               consumer-group_test01-1-ef8979ba-4696-49b2-b26c-949deb2fd576 /192.168.3.214  consumer-group_test01-1
     * group_test01    test            2          0               0               0               consumer-group_test01-1-ef8979ba-4696-49b2-b26c-949deb2fd576 /192.168.3.214  consumer-group_test01-1
     */
    @Test
    public void testConsumer() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop02:9092,hadoop03:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_test01");
        // 默认为true，自动提交
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        // 手动提交，需要配合
        // config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);

        try (KafkaConsumer<Integer, String> consumer = new KafkaConsumer<>(config)) {
            consumer.subscribe(singleton("test"));
            // 这里参考官网的代码，不要直接写true
            while (true) {
                ConsumerRecords<Integer, String> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<Integer, String> record : records) {
                    log.info("消息内容:{}", record);
                }
                // 手动异步提交 需要将 ENABLE_AUTO_COMMIT_CONFIG 配置为false
                // consumer.commitAsync();
                // 手动同步提交 需要将 ENABLE_AUTO_COMMIT_CONFIG 配置为false
                // consumer.commitSync();
            }
        } catch (Throwable e) {
            log.error("consumer error:", e);
        }
    }
}
