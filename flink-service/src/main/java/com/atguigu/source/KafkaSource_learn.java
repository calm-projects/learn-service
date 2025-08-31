package com.atguigu.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
public class KafkaSource_learn {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    public static class Orders {
        public Integer orderId;
        public String orderName;
        public LocalDateTime orderTime;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger(RestOptions.PORT, 8081);
        // StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);
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

          数据如下：
          kafka-topics.sh --bootstrap-server hadoop03:9092 --create --topic orders --partitions 2 --replication-factor 2

          生产数据
          kafka-console-producer.sh --bootstrap-server hadoop03:9092 --topic orders
          {"orderId":1,"orderName":"苹果","orderTime":"2025-08-19 09:31:03"}
          {"orderId":1,"orderName":"蓝莓","orderTime":"2025-08-19 09:31:05"}
          {"orderId":1,"orderName":"桑葚","orderTime":"2025-08-19 09:31:06"}
          {"orderId":1,"orderName":"柿子","orderTime":"2025-08-19 09:31:07"}
          {"orderId":1,"orderName":"樱桃","orderTime":"2025-08-19 09:31:08"}

          消费数据(测试控制台和代码都可以看一看)
          kafka-console-consumer.sh --bootstrap-server hadoop03:9092 --topic orders --from-beginning


        flink提交命令相关
        这里这样设置jm和tm内存是因为我的yarn最小和最大内存分别为512 和 1024，jm和tm设置太大了会报错内存不够用，设置-p间接测试了下代码里面设置的并行度比命令行优先级要高
        还有命令行最好按照这个顺序写
        flink run-application -t yarn-application -d -Djobmanager.memory.process.size=1024m -Dtaskmanager.memory.process.size=1024m -Dclassloader.resolve-order=parent-first -p 1 -c com.learn.java.source.KafkaSource_learn flink-test-1.0-SNAPSHOT.jar
        yarn logs -applicationId application_1755652974479_0008
        flink stop --savepointPath hdfs://hadoop01:8020/flink/savepoint a75af58cf25f40df006fcfc3119ad548
        flink run-application -t yarn-application -d -Djobmanager.memory.process.size=1024m -Dtaskmanager.memory.process.size=1024m -Dclassloader.resolve-order=parent-first -s hdfs://hadoop01:8020/flink/chk/c97913ff0cf4a8dc1570f40632cb5e4d/chk-50 -p 1 -c com.learn.java.source.KafkaSource_learn flink-test-1.0-SNAPSHOT.jar

        报错解决：
        Caused by: java.lang.ClassCastException: cannot assign instance of org.apache.kafka.clients.consumer.OffsetResetStrategy to field org.apache.flink.connector.kafka.source.enumerator.initializer.ReaderHandledOffsetsInitializer.offsetResetStrategy of type org.apache.kafka.clients.consumer.OffsetResetStrategy in instance of org.apache.flink.connector.kafka.source.enumerator.initializer.ReaderHandledOffsetsInitializer
        flink运行命令添加 -Dclassloader.resolve-order=parent-first
         */

        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        // 指定检查点的存储位置,指定hadoop用户
        System.setProperty("HADOOP_USER_NAME", "hadoop");
        checkpointConfig.setCheckpointStorage("hdfs://hadoop01:8020/flink/chk");
        // 这里我们如果要测试flink checkpoint的左右需要设置，不然每次cancel或者stop会将checkpoint删除掉，很多配置在flink文档都介绍了这里不赘述
        checkpointConfig.setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        // 本来只是想在idea里面测试，不提交命令行到yarn了，发现增加后直接报错了，从checkpoint或者savepoint启动有些代码的修改是不支持的
        // env.setDefaultSavepointDirectory("/flink/chk/3228ebcf280b39a4f45b81a652d67c41/chk-9");

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("hadoop02:9092,hadoop03:9092")
                .setTopics("orders")
                .setGroupId("group08")
                /*
                    TODO 简单介绍下重启如何读取偏移量信息的
                    1 存在checkpoint 应用重启
                    1.1 指定-s checkpoint路径 并且设置偏移量为：setStartingOffsets(OffsetsInitializer.earliest())
                        会读取checkpoint中消费的offsets继续消费数据
                    1.2 不指定-s checkpoint路径 并且设置偏移量为：setStartingOffsets(OffsetsInitializer.earliest())
                        会重新开始消费数据

                    1.3 不指定-s checkpoint路径 并且设置偏移量为：OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST)
                        从kafka中消费数据
                    1.4 指定-s checkpoint路径 并且设置偏移量为：OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST)
                        从checkpoint存储的偏移量开始消费数据，这个是验证过的，验证过程如下（除了指定-s其余都可以本地）：
                        step1：部署application到yarn，然后cancel(pro 请使用优雅的停止stop命令)，拿到一个checkpoint
                        step2: 本地启动application，再往kafka写一条数据，这时候kafka的offsets比checkpoint的offsets多消费一条数据,kafka存储的offsets如下
                            [hadoop@hadoop03 ~]$ kafka-consumer-groups.sh --bootstrap-server hadoop03:9092 --group group08 --describe

                            Consumer group 'group08' has no active members.

                            GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
                            group08         orders          0          9               9               0               -               -               -
                            group08         orders          1          0               0               0               -               -               -
                        step3：再次部署application到yarn，指定-s 保存点为checkpoint的路径，观察是否消费9这条数据 确实重新消费了，所以是读取的checkpoint


                    2 不存在checkpoint 应用重启
                    2.1 不设置 .setProperty("enable.auto.commit", "true") 和 .setProperty("auto.commit.interval.ms", "3")
                        无论是OffsetsInitializer.earliest()还是OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST)
                        都无法根据消费的offsets消费数据，因为根本不会记录消费信息到kafka
                    2.2 设置 .setProperty("enable.auto.commit", "true") 和 .setProperty("auto.commit.interval.ms", "3")
                        设置偏移量为：OffsetsInitializer.earliest()，还是从earliest开始消费
                        设置偏移量为: KafkaRecordDeserializationSchema.valueOnly(StringDeserializer.class) 则会读取kafka 消费组的消费偏移量消费消费数据

                 */
//                .setStartingOffsets(OffsetsInitializer.earliest())
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
                // .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(StringDeserializer.class))
                // 仅仅反序列化value，其他的会被忽略
                .setValueOnlyDeserializer(new SimpleStringSchema())
                /*
                    当我们topic增加了分区，或者我们读取topic设置的是通配符，这样flink不会自动识别新分区和新topic，
                    可以设置这个选项，每 10 秒检查一次新分区，如果不增加分区和使用通配符建议不要开启，肯定增加线程消耗 默认不开启
                 */
                .setProperty("partition.discovery.interval.ms", "10000")
                /*
                    默认true 默认5秒
                    TODO ***开启checkpoint后不需要设置下面配置也会往kafka写消费信息****，且使用的是flink指定的groupId
                    TODO 不开启checkpoint 且不设置下面俩个配置，默认不往kafka提交偏移量,而且kafka查询不到消费者组，消费者组直接以
                        console-consumer-xxxx 启动的 无论偏移量设置为
                        setStartingOffsets(OffsetsInitializer.earliest())
                        还是
                        OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST)
                        都不会往kafka写消费信息，除非开启了下面的这俩个配置
                 */
                .setProperty("enable.auto.commit", "true")
                .setProperty("auto.commit.interval.ms", "3")
                .build();

        /*
            默认情况下，Kafka Source 使用 Kafka 消息中的时间戳作为事件时间。可以定义自己的水印策略（Watermark Strategy）
            env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");
            env.fromSource(source, WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(3)), "Kafka Source");
            一般我们都自定义，不用这个
         */
        DataStreamSource<String> ordersDs = env.fromSource(
                source,
                WatermarkStrategy
                        // 这里必须定义泛型
                        .<String>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                        // 可以修改为lambda，直接idea快捷键即可，这里我们保留原样
                        .withTimestampAssigner(new SerializableTimestampAssigner<String>() {
                            @Override
                            public long extractTimestamp(String element, long recordTimestamp) {
                                try {
                                    Orders orders = objectMapper.readValue(element, Orders.class);
                                    return orders.getOrderTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                } catch (IOException e) {
                                    /*
                                        一般不会报错，报错可以直接抛出异常,也可以设置最小时间，不影响 watermark，最好不要设置当前时间
                                        还可以设置标志位，然后过滤处理，
                                     */
                                    log.error("extractTimestamp error:", e);
                                    return Long.MIN_VALUE;
                                }
                            }
                        })
                        .withIdleness(Duration.ofSeconds(3)),
                "Kafka_Source");


        ordersDs.print().uid("print");
        env.execute();
    }
}
