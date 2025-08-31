package com.atguigu.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.math.BigDecimal;

public class FileSystemSource_learn {
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

        tableEnv.executeSql("CREATE TABLE fs_table (\n" +
                "  user_id STRING,\n" +
                "  order_amount decimal(10,2),\n" +
                "  ts integer,\n" +
                "  rowtime TIMESTAMP_LTZ(3),\n" +
                "  dt STRING,\n" +
                "  `hour` STRING,\n" +
                "  `file.path` STRING NOT NULL METADATA\n" +
                ") PARTITIONED BY (dt, `hour`) WITH (\n" +
                "  'connector'='filesystem',\n" +
                "  'path'='./data/flink/output',\n" +
                "  'source.monitor-interval'='10 s',\n" +
                "  'format'='parquet'\n" +
                ")");

        tableEnv.executeSql("select * from fs_table").print();
    }
}
