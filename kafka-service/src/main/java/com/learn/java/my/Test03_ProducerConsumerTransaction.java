package com.learn.java.my;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.singleton;


@Slf4j
public class Test03_ProducerConsumerTransaction {
    /**
     * 消费信息：
     * kafka-console-consumer.sh --bootstrap-server hadoop02:9092 --topic test --from-beginning
     * 查看偏移量信息:
     * kafka-run-class.sh kafka.tools.GetOffsetShell --topic test --bootstrap-server hadoop02:9092
     * 查看log文件
     * kafka-run-class.sh kafka.tools.DumpLogSegments --files /opt/service/kafka/kafka-logs/test-1/00000000000000000000.log --max-message-size 10 --print-data-log
     *
     * 不同的生产者向同一个topic写消息，生产者事务id需要不同
     */
    @Test
    public void testProducer() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop02:9092,hadoop03:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        // 开启事务必须开启幂等性，幂等性是默认开启的
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-tx-id");
        config.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 10_000);
        // 里面还有好多其他配置，比如幂等性、batch.size、buffer.memory等等
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(config)) {
            producer.initTransactions();
            try {
                producer.beginTransaction();
                for (int i = 0; i < 5; i++) {
                    ProducerRecord<String, String> record = new ProducerRecord<>(
                            "test", 2, null, "hello-" + i
                    );
                    producer.send(record);
                }
                producer.abortTransaction();
            } catch (KafkaException e) {
                producer.abortTransaction();
                log.error("事务回滚: ", e);
            }
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
     *
     * 这里提交偏移量还是可以自动和手动，只是增加了一个事务过滤而已（已提交读，未提交读）
     */
    @Test
    public void testConsumer() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop02:9092,hadoop03:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_test01");
        // 配置事务隔离级别
        // - read_committed: 只读取已提交的消息
        // - read_uncommitted: 读取所有消息（包括未提交的）
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        // 手动提交偏移量（与事务配合使用）
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

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
                // 手动提交偏移量（非事务性提交）
                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }
        } catch (Throwable e) {
            log.error("consumer error:", e);
        }
    }


    @Test
    void testProductConsumerWithTransaction() {
        // 消费者配置
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop02:9092,hadoop03:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group_test01");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 5);

        // 生产者配置（支持事务）
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop02:9092,hadoop03:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-tx-id");
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        producerProps.put(ProducerConfig.ACKS_CONFIG, "all");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
             KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {

            // 初始化生产者事务
            producer.initTransactions();
            consumer.subscribe(Collections.singletonList("test"));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

                if (!records.isEmpty()) {
                    try {
                        producer.beginTransaction();

                        for (ConsumerRecord<String, String> record : records) {
                            log.info("消费内容:{}", record);
                            producer.send(new ProducerRecord<>("test2", record.key(), record.value()));
                        }

                        // 事务性提交偏移量（与生产操作绑定在同一事务）,利用生产者事务提交消费者偏移量
                        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
                        for (TopicPartition partition : consumer.assignment()) {
                            offsets.put(partition, new OffsetAndMetadata(consumer.position(partition)));
                        }
                        producer.sendOffsetsToTransaction(offsets, consumer.groupMetadata());

                        producer.commitTransaction();
                    } catch (KafkaException e) {
                        producer.abortTransaction();
                        log.error("事务处理失败:", e);
                    }
                }
            }
        }
    }
}