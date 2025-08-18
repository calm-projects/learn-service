package com.atguigu.source;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.impl.StreamFormatAdapter;
import org.apache.flink.connector.file.src.reader.StreamFormat;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.formats.parquet.avro.AvroParquetReaders;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

public class FileSourceDemo2 {
    @Data(staticConstructor = "of")
    @Accessors(chain = true)
    static class User {
        public String name;
        public int age;
    }
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8082);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        env.setParallelism(1);

       /*
        1. 按行读取
            FileSource<String> fileSource = FileSource.forRecordStreamFormat(
                    new TextLineInputFormat(),
                    new Path("data/flink/input/user.json")
            ).build();
            DataStreamSource<String> ds = env.fromSource(fileSource, WatermarkStrategy.noWatermarks(), "file-source");
        */

        // 读取avro parquet文件
        StreamFormat<User> format = AvroParquetReaders.forReflectRecord(User.class);
        FileSource<User> fileSource = FileSource
                .forBulkFileFormat(new StreamFormatAdapter<>(format), new Path("data/flink/output"))
                // 持续监控 batch 转 stream
                .monitorContinuously(Duration.ofSeconds(10))
                .build();
        DataStreamSource<User> ds = env.fromSource(fileSource, WatermarkStrategy.noWatermarks(), "parquet-source");
        ds.print();
        env.execute();
    }
}
