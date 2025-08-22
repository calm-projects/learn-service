package com.atguigu.sink;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkFixedPartitioner;
import org.apache.kafka.clients.producer.ProducerConfig;

public class KafkaSink_learn {
    @Data(staticConstructor = "of")
    @Accessors(chain = true)
    public static class User{
        private String id;
        private String name;
    }
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8081);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);
        env.enableCheckpointing(10*1000, CheckpointingMode.EXACTLY_ONCE);
        // 指定检查点的存储位置,指定hadoop用户
        System.setProperty("HADOOP_USER_NAME", "hadoop");
        env.getCheckpointConfig().setCheckpointStorage("hdfs://hadoop01:8020/flink/chk");
        env.getRestartStrategy();


        SingleOutputStreamOperator<String> socketDs = env.socketTextStream("hadoop03", 8888).uid("socket-source");
        socketDs.print().uid("socket-print");
        /*
            kafka命令相关
            创建topic:
            kafka-topics.sh --bootstrap-server hadoop03:9092 --create --topic topic-01 --partitions 3 --replication-factor 2
            查看topic偏移量(kafka-run-class.sh帮助信息极少，直接去github kafka tools下看对应的class代码即可，比如GetOffsetShell类):
            kafka-run-class.sh kafka.tools.GetOffsetShell --topic topic-01 --bootstrap-server hadoop02:9092
            消费信息:
            kafka-console-consumer.sh --bootstrap-server hadoop03:9092 --topic topic-01 --from-beginning --isolation-level read_committed --property print.key=true
         */

        /*
            至少一次 仅仅包含value
            KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                    .setBootstrapServers("hadoop02:9092,hadoop03:9092")
                    .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                            .setTopic("topic-01")
                            // 默认采用的round-robin
                            .setPartitioner(new FlinkFixedPartitioner<>())
                            .setValueSerializationSchema(new SimpleStringSchema())
                            .build()
                    ).setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                    // 下面这俩个底层是一样的
                    .setProperty(ProducerConfig.ACKS_CONFIG, "all")
                    // .setKafkaProducerConfig()
                    .build();
             socketDs.sinkTo(kafkaSink).uid("kafka-sink");
         */

        /*
            至少一次 仅仅包含 key 和 value 下面的请改用lambda 这里因为是学习所以写的是匿名函数
            SingleOutputStreamOperator<User> userDs = socketDs.map((MapFunction<String, User>) value -> {
                String[] split = value.split(",");
                return User.of().setId(split[0]).setName(split[1]);
            }).uid("map-user");
            KafkaSink<User> kafkaSink = KafkaSink.<User>builder()
                    .setBootstrapServers("hadoop02:9092,hadoop03:9092")
                    .setRecordSerializer(
                            KafkaRecordSerializationSchema.builder()
                                    .setTopic("topic-01")
                                    // 默认采用的round-robin
                                    .setPartitioner(new FlinkFixedPartitioner<>())
                                    // key: 直接 new 一个匿名类
                                    .setKeySerializationSchema(new SerializationSchema<User>() {
                                        @Override
                                        public byte[] serialize(User element) {
                                            return element.getId().getBytes(StandardCharsets.UTF_8);
                                        }
                                    })
                                    // value: 直接 new 一个匿名类
                                    .setValueSerializationSchema(new SerializationSchema<User>() {
                                        @Override
                                        public byte[] serialize(User element) {
                                            return element.toString().getBytes(StandardCharsets.UTF_8);
                                        }
                                    })
                                    .build()
                    )
                    .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                    .setProperty(ProducerConfig.ACKS_CONFIG, "all")
                    .build();
            userDs.sinkTo(kafkaSink).uid("kafka-sink");
         */

        // 精确一次性提交，如果不开启checkpoint是不会提交事务的，
        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers("hadoop02:9092,hadoop03:9092")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("topic-01")
                        // 默认采用的round-robin
                        .setPartitioner(new FlinkFixedPartitioner<>())
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .setTransactionalIdPrefix("topic-01-tx")
                // kafka事务超时时间，因为每次checkpoint后才会提交事务，应该将此值调整至远大于 checkpoint 最大间隔 + 最大重启时间(默认重启策略不重启)，并且要小于kafka的最大超时时间 15min
                .setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 10 * 60 * 1000 + "")
                // 下面这俩个底层是一样的
                .setProperty(ProducerConfig.ACKS_CONFIG, "all")
                // .setKafkaProducerConfig()
                .build();
        socketDs.sinkTo(kafkaSink).uid("kafka-sink");

        env.execute();
    }
}
