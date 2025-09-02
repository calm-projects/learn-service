package com.atguigu.sql;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class KafkaSourceSink_learn {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setInteger(RestOptions.PORT, 8081);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(3);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);


        /*
            kafka命令相关
            创建topic:
            kafka-topics.sh --bootstrap-server hadoop03:9092 --create --topic order_infos --partitions 3 --replication-factor 2

            生产数据
              kafka-console-producer.sh --bootstrap-server hadoop03:9092 --topic order_infos
              {"orderId":1,"orderName":"苹果","orderTime":"2025-08-19 09:31:03"}
              {"orderId":1,"orderName":"蓝莓","orderTime":"2025-08-19 09:31:05"}
              {"orderId":1,"orderName":"桑葚","orderTime":"2025-08-19 09:31:06"}
              {"orderId":1,"orderName":"柿子","orderTime":"2025-08-19 09:31:07"}
              {"orderId":1,"orderName":"樱桃","orderTime":"2025-08-19 09:31:08"}

            查看topic偏移量(kafka-run-class.sh帮助信息极少，直接去github kafka tools下看对应的class代码即可，比如GetOffsetShell类):
            kafka-run-class.sh kafka.tools.GetOffsetShell --topic order_infos --bootstrap-server hadoop02:9092
            消费信息:
            kafka-console-consumer.sh --bootstrap-server hadoop03:9092 --topic order_infos --from-beginning --isolation-level read_committed --property print.key=true

         */
        tableEnv.executeSql("CREATE TABLE KafkaTable (\n" +
                "  `event_time` TIMESTAMP_LTZ(3) METADATA FROM 'timestamp',\n" +
                "  `partition` BIGINT METADATA VIRTUAL,\n" +
                "  `offset` BIGINT METADATA VIRTUAL,\n" +
                "  `orderId` BIGINT,\n" +
                "  `orderName` STRING,\n" +
                "  `orderTime` STRING\n" +
                ") WITH (\n" +
                "  'connector' = 'kafka',\n" +
                "  'topic' = 'order_infos',\n" +
                "  'properties.bootstrap.servers' = 'hadoop03:9092',\n" +
                "  'properties.group.id' = 'testGroup',\n" +
                "  'scan.startup.mode' = 'earliest-offset',\n" +
                "  'format' = 'json'\n" +
                ")");

        tableEnv.executeSql("CREATE TABLE kafkaSink (\n" +
                "  `event_time` TIMESTAMP_LTZ(3) METADATA FROM 'timestamp',\n" +
                "  `partition` BIGINT METADATA VIRTUAL,\n" +
                "  `offset` BIGINT METADATA VIRTUAL,\n" +
                "  `orderId` BIGINT,\n" +
                "  `orderName` STRING,\n" +
                "  `orderTime` STRING\n" +
                ") WITH (\n" +
                "  'connector' = 'kafka',\n" +
                "  'topic' = 'order_infos_01',\n" +
                "  'properties.bootstrap.servers' = 'hadoop03:9092',\n" +
                "  'format' = 'json'\n" +
                ")");

        // print阻塞执行
        // tableEnv.executeSql("select * from KafkaTable").print();
         tableEnv.executeSql("insert into kafkaSink select event_time, orderId, orderName,orderTime from KafkaTable");

    }
}
