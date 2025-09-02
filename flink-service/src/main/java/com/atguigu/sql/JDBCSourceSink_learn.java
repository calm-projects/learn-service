package com.atguigu.sql;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class JDBCSourceSink_learn {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.setInteger(RestOptions.PORT, 8081);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(3);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        // 注意user 是内置单词起名字要避开，mysql中 BIGINT UNSIGNED 对应的是flink的 DECIMAL(20, 0)
        tableEnv.executeSql("CREATE TABLE source_user (\n" +
                "  id DECIMAL(20, 0),\n" +
                "  name STRING,\n" +
                "  age INT,\n" +
                "  PRIMARY KEY (id) NOT ENFORCED\n" +
                ") WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = 'jdbc:mysql://localhost:3306/test',\n" +
                "   'table-name' = 'user',\n" +
                "   'username' = 'root',\n" +
                "   'password' = 'root'\n" +
                ")");

        // 需要在mysql创建表
        tableEnv.executeSql("CREATE TABLE sink_user_back (\n" +
                "  id DECIMAL(20, 0),\n" +
                "  name STRING,\n" +
                "  age INT,\n" +
                "  PRIMARY KEY (id) NOT ENFORCED\n" +
                ") WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = 'jdbc:mysql://localhost:3306/test',\n" +
                "   'table-name' = 'user_back',\n" +
                "   'username' = 'root',\n" +
                "   'password' = 'root'\n" +
                ")");

        tableEnv.executeSql("CREATE TABLE sink_user_static (\n" +
                "  name STRING,\n" +
                "  cnt BIGINT,\n" +
                "  PRIMARY KEY (name) NOT ENFORCED\n" +
                ") WITH (\n" +
                "   'connector' = 'jdbc',\n" +
                "   'url' = 'jdbc:mysql://localhost:3306/test',\n" +
                "   'table-name' = 'user_static',\n" +
                "   'username' = 'root',\n" +
                "   'password' = 'root'\n" +
                ")");
        // tableEnv.executeSql("select * from source_user").print();
        // 会自动根据id做upsert操作
        // tableEnv.executeSql("insert into sink_user_back select * from source_user");
        // tableEnv.sqlQuery("select name, count(1) as cnt from source_user group by name").execute().print();
        tableEnv.executeSql("insert into sink_user_static(name, cnt) select name, count(1) as cnt from source_user group by name");
    }
}
