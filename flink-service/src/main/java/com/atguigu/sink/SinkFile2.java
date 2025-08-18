package com.atguigu.sink;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class SinkFile2 {
    @Data(staticConstructor = "of")
    @Accessors(chain = true)
    private static class User {
        private String name;
        private int age;
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
        env.setParallelism(2);

        /*
            1 batch模式
            DataStreamSource<User> userDs = env.fromElements(
                    User.of().setName("jack").setAge(18),
                    User.of().setName("tom").setAge(8),
                    User.of().setName("rose").setAge(18));
         */

        // 2 stream 模式
        // stream 模式下必须开启checkpoint 否则文件一直处于pending状态 无法转为finished状态
        env.enableCheckpointing(10 * 1000, CheckpointingMode.EXACTLY_ONCE);
        SingleOutputStreamOperator<User> userDs = env.socketTextStream("hadoop03", 8888)
                .map((MapFunction<String, User>) value -> {
                    String[] split = value.split(",");
                    return User.of().setName(split[0]).setAge(Integer.parseInt(split[1]));
                });
        FileSink<User> sink = FileSink
                .forRowFormat(new Path("output/file/"), new SimpleStringEncoder<User>(StandardCharsets.UTF_8.name()))
                .withBucketAssigner(new DateTimeBucketAssigner<>("yyyy-MM-dd--HH"))
                .withOutputFileConfig(
                        OutputFileConfig.builder()
                                .withPartPrefix("flink")
                                .withPartSuffix(".log")
                                .build()
                ).withRollingPolicy(
                        /*
                            不同状态文件名称如下：
                                In-progress / Pending：part-<uid>-<partFileIndex>.inprogress.uid partFileIndex如果有多个文件比如 0 1 2，那么01都是pending状态
                                Finished：part-<uid>-<partFileIndex>
                            滚动策略如下：
                                文件大小达到1M || 接受了60秒数据 || 120内未接受到新数据
                            为什么字节和时间都达到了且pending了，但是文件并没有finished呢？
                                需要设置checkpoint不然不进行finished
                         */
                        DefaultRollingPolicy.builder()
                                // 文件达到10字节滚动
                                .withMaxPartSize(new MemorySize(1024 * 1024))
                                // 文件
                                .withRolloverInterval(Duration.ofSeconds(60))
                                .withInactivityInterval(Duration.ofSeconds(120))
                                .build()
                ).build();

        userDs.sinkTo(sink);
        env.execute();
    }
}


