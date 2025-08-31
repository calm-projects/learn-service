package com.atguigu.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.math.BigDecimal;
import java.time.Duration;

public class FileSystemSink_learn {
    /**
     * 订单类
     * 权限最好是public，否则会自动识别为Row, 和定义.returns(Order.class)无关，flink会自动识别
     * 字段顺序由构造器或字母序决定
     */
    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        private String userId;
        private BigDecimal orderAmount;
        private Integer ts;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        // configuration.setInteger(RestOptions.PORT, 8081);
        configuration.setString(RestOptions.BIND_PORT, "8081-8090");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(3);
        env.enableCheckpointing(5000);
        env.getCheckpointConfig().setCheckpointStorage("file:///Users/calm/project/temp/flink-test/data/checkpoints");
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        /*
            1,1.5,1
            1,1.2,2
            1,1.2,3
            1,1.2,4
            1,1.2,5
            1,1.2,6
            1,1.2,7
            1,1.2,12
            1,1.2,22
            1,1.2,3600
            1,1.2,3900
            1,1.2,7200
            1,1.2,7300
            1,1.2,9300
            1,1.2,9700
            1,1.2,10900
         */
        SingleOutputStreamOperator<Order> orderDS = env
                .socketTextStream("localhost", 8888).uid("source_socket")
                .map((MapFunction<String, Order>) value -> {
                    String[] split = value.split(",");
                    return new Order().setUserId(split[0]).setOrderAmount(new BigDecimal(split[1])).setTs(Integer.parseInt(split[2]));
                }).uid("source_map")
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<Order>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                                .withTimestampAssigner(new SerializableTimestampAssigner<Order>() {
                                    @Override
                                    public long extractTimestamp(Order element, long recordTimestamp) {
                                        return element.getTs() * 1000;
                                    }
                                })).returns(Order.class).uid("watermark_assign");


        tableEnv.executeSql("CREATE TABLE fs_table (\n" +
                "  user_id STRING,\n" +
                "  order_amount decimal(10,2),\n" +
                "  ts integer,\n" +
                "  rowtime TIMESTAMP_LTZ(3),\n" +
                "  dt STRING,\n" +
                "  `hour` STRING\n" +
                ") PARTITIONED BY (dt, `hour`) WITH (\n" +
                "  'connector'='filesystem',\n" +
                "  'path'='./data/flink/output',\n" +
                "  'format'='parquet',\n" +
                "  'partition.time-extractor.timestamp-pattern'='$dt $hour:00:00',\n" +
                "  'sink.partition-commit.delay'='1 h',\n" +
                "  'sink.partition-commit.trigger'='partition-time',\n" +
                "  'sink.partition-commit.watermark-time-zone'='Asia/Shanghai', -- 假设用户配置的时区为 'Asia/Shanghai'\n" +
                "  'sink.partition-commit.policy.kind'='success-file'\n" +
                ")");

        /*
            flink 会自动识别pojo类，Query schema: [userId: STRING, orderAmount: DECIMAL(38, 18), ts: INT]
            我们也可以定义table 的schema，
            TypeInformation to DataType # BigDecimal is converted to DECIMAL(38, 18) by default.
         */
        Table table = tableEnv.fromDataStream(orderDS, Schema.newBuilder()
                .column("userId", "string")
                .column("orderAmount", "decimal(10,2)")
                .column("ts", "integer")
                .columnByMetadata("rowtime", "TIMESTAMP_LTZ(3)")
                .watermark("rowtime", "SOURCE_WATERMARK()")
                .build()).as("user_id", "order_amount", "ts", "rowtime");
        table.printSchema();
        tableEnv.createTemporaryView("orders", table);
        tableEnv.executeSql("insert into fs_table " +
                "select user_id, order_amount, ts, rowtime," +
                "DATE_FORMAT(TO_TIMESTAMP_LTZ(ts*1000, 3), 'yyyy-MM-dd') as dt, " +
                "DATE_FORMAT(TO_TIMESTAMP_LTZ(ts*1000, 3), 'HH') as `hour` from orders");
    }
}
