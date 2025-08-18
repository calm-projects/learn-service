package com.atguigu.sink;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.sink.compactor.DecoderBasedReader;
import org.apache.flink.connector.file.sink.compactor.FileCompactStrategy;
import org.apache.flink.connector.file.sink.compactor.RecordWiseFileCompactor;
import org.apache.flink.connector.file.sink.compactor.SimpleStringDecoder;
import org.apache.flink.core.fs.Path;
import org.apache.flink.formats.avro.AvroWriters;
import org.apache.flink.formats.parquet.avro.AvroParquetReaders;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;

public class SinkFile3Avro {
    @Data(staticConstructor = "of")
    @Accessors(chain = true)
    static class User {
        public String name;
        public int age;

    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
        env.setParallelism(1);
        /*
            1 batch模式
            DataStreamSource<User> userDs = env.fromElements(
                    User.of().setName("jack").setAge(18),
                    User.of().setName("tom").setAge(8),
                    User.of().setName("rose").setAge(18));
         */

        // 2 stream模式
        // stream 模式下必须开启checkpoint 否则文件一直处于pending状态 无法转为finished状态
        env.enableCheckpointing(10 * 1000, CheckpointingMode.EXACTLY_ONCE);
        SingleOutputStreamOperator<User> userDs = env.socketTextStream("localhost", 8888).uid("socket-source")
                .map((MapFunction<String, User>) value -> {
                    String[] split = value.split(",");
                    return User.of().setName(split[0]).setAge(Integer.parseInt(split[1]));
                }).uid("map");

        /*
            滚动策略可以看下源码，只有checkpoint的时候才会滚动，如果这样会造成大量小文件，这肯定不可以，如何解决呢？
                1 减少并行度 2 增加checkpoint时长 3后置合并 可以使用数据湖Hudi/Iceberg/DeltaLake
            为什么bulk不支持 size 和 time滚动策略呢？
                Bulk 格式（Parquet、Avro、ORC 等）是 分块/列式存储，必须在写完一个完整文件之后，才保证是合法文件。

            notes：1.15后增加了压缩，
            压缩策略有俩个
                    1 个是当文件大小达到了阈值
                    2 通过的检查点数量达到指定个数
            小文件合并方式：
                OutputStreamBasedFileCompactor 直接对文件做字节级的拼接（stream copy），不关心里面的数据结构。
                    只能用在 拼接后仍然是合法文件 的场景，比如纯文本文件、Avro Container File 等。
                RecordWiseFileCompactor 逐条读出小文件里的数据记录（record），再写到新文件。
                    能保证目标文件绝对合法（因为是重新写一遍），适合有 schema / footer 的复杂格式（Parquet、ORC）。


         */
        FileSink<User> sink = FileSink
                .forBulkFormat(new Path("output/file/"), AvroWriters.forReflectRecord(User.class))
                .withBucketAssigner(new DateTimeBucketAssigner<>("yyyy-MM-dd--HH"))
                .withOutputFileConfig(
                        OutputFileConfig.builder()
                                .withPartPrefix("flink")
                                .withPartSuffix(".log")
                                .build()
                ).withRollingPolicy(
                        OnCheckpointRollingPolicy.build()
                ).enableCompact(
                        FileCompactStrategy.Builder.newBuilder()
                                .setSizeThreshold(1024)
                                .enableCompactionOnCheckpoint(5)
                                .build(),
                        new RecordWiseFileCompactor<>(
                                AvroReaders.forReflectRecord(User.class)))
                .build();

        userDs.sinkTo(sink).uid("file-sink");
        env.execute();
    }
}
