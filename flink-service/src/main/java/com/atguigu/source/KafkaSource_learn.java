package com.atguigu.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.LocalDateTime;

public class KafkaSource_learn {
    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static class Orders{
        public Integer orderId;
        public String orderName;
        public LocalDateTime orderTime;
    }
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8081);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(2);

        /*
        设置topic的方式：
        1. KafkaSource.builder().setTopics("topic-a", "topic-b");
        2. KafkaSource.builder().setTopicPattern("topic.*");
        3. final HashSet<TopicPartition> partitionSet = new HashSet<>(Arrays.asList(
                new TopicPartition("topic-a", 0),    // Partition 0 of topic "topic-a"
                new TopicPartition("topic-b", 5)));  // Partition 5 of topic "topic-b"
            KafkaSource.builder().setPartitions(partitionSet);

         设置topic偏移量：
         KafkaSource.builder()
            // 从消费组提交的位点开始消费，不指定位点重置策略
            .setStartingOffsets(OffsetsInitializer.committedOffsets())
            // 从消费组提交的位点开始消费，如果提交位点不存在，使用最早位点
            .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
            // 从时间戳大于等于指定时间戳（毫秒）的数据开始消费
            .setStartingOffsets(OffsetsInitializer.timestamp(1657256176000L))
            // 从最早位点开始消费， 默认的
            .setStartingOffsets(OffsetsInitializer.earliest())
            // 从最末尾位点开始消费
            .setStartingOffsets(OffsetsInitializer.latest());

          设置停止消费的offsets
          不管batch还是stream都可以设置消费的停止消费的offsets,常用于读取一段历史数据
          batch模式：setBounded(OffsetsInitializer)
          stream模式：setUnbounded(OffsetsInitializer)

         */

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("hadoop02:9092,hadoop03:9092")
                .setTopics("test")
                .setGroupId("group01")
                .setStartingOffsets(OffsetsInitializer.earliest())
                // .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(StringDeserializer.class))
                // 仅仅反序列化value，其他的会被忽略
                .setValueOnlyDeserializer(new SimpleStringSchema())
                /*
                    当我们topic增加了分区，或者我们读取topic设置的是通配符，这样flink不会自动识别新分区和新topic，
                    可以设置这个选项，每 10 秒检查一次新分区，如果不增加分区和使用通配符建议不要开启，肯定增加线程消耗 默认不开启
                 */
                .setProperty("partition.discovery.interval.ms", "10000")
                .build();

        /*
            默认情况下，Kafka Source 使用 Kafka 消息中的时间戳作为事件时间。可以定义自己的水印策略（Watermark Strategy）
            env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");
            env.fromSource(source, WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(3)), "Kafka Source");
            一般我们都自定义，不用这个
         */
        env.fromSource(
                source,
                WatermarkStrategy
                        .<String>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                        .withTimestampAssigner(new SerializableTimestampAssigner<String>() {
                            @Override
                            public long extractTimestamp(String element, long recordTimestamp) {
                                try {
                                    objectMapper.readValue(element, Orders.class);
                                    return 0L;
                                } catch (JsonProcessingException e) {
                                    // 可以打印个异常 设置 最小时间 不影响 watermark 然后在filter过滤写到侧输出流
                                    return Long.MIN_VALUE;
                                }
                            }
                        })
                        .withIdleness(Duration.ofSeconds(30)),
    "Kafka_Source");

        env.execute();
    }
}
