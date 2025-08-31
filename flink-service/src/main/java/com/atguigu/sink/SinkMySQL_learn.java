package com.atguigu.sink;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class SinkMySQL_learn {
    @Data(staticConstructor = "of")
    @Accessors(chain = true)
    private static class User {
        private Integer id;
        private String name;
        private Integer age;
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger(RestOptions.PORT, 8081);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(2);
        SingleOutputStreamOperator<User> userDs = env
                .socketTextStream("hadoop03", 8888).uid("source_socket")
                .map((MapFunction<String, User>) value -> {
                    String[] split = value.split(",");
                    return User.of().setId(Integer.parseInt(split[0])).setName(split[1]).setAge(Integer.parseInt(split[2]));
                }).uid("source_map");
        /*
            create table user(id int primary key , name varchar(10), age int)
            notes: 正常创建表的时候要规范，这里只是本地测试
            发现sql写错了，不报错，而且taskManager也没有任何错误信息
         */
        SinkFunction<User> jdbcSink = JdbcSink.sink(
                "insert into user(id, name, age) values(?,?,?) on duplicate key update name=VALUES(name), age=VALUES(age)",
                (JdbcStatementBuilder<User>) (ps, user) -> {
                    ps.setInt(1, user.getId());
                    ps.setString(2, user.getName());
                    ps.setInt(3, user.getAge());
                },
                /*
                    失败时最多重试 3 次
                    批量写入 100 条记录再执行一次 SQL
                    即使不足 100 条，也会每 3 秒写一次
                 */
                JdbcExecutionOptions.builder()
                        .withMaxRetries(3)
                        .withBatchSize(100)
                        .withBatchIntervalMs(3000)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:mysql://localhost:3306/test?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8")
                        .withUsername("root")
                        .withPassword("root")
                        // 检查连接的超时时间 默认60s
                        .withConnectionCheckTimeoutSeconds(60)
                        .build()
        );
        userDs.addSink(jdbcSink);
        env.execute();
    }
}
