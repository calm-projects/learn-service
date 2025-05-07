package com.calm.mr.test07_outputFormat;

import com.calm.data.Paths;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;


@Slf4j
public class LogOutputFormat extends FileOutputFormat<Text, NullWritable> {

    @Slf4j
    static class LogRecordWriter extends RecordWriter<Text, NullWritable> {

        private FSDataOutputStream mainOutput;
        private FSDataOutputStream otherOut;

        public LogRecordWriter(TaskAttemptContext job) {
            try {
                FileSystem fs = FileSystem.get(job.getConfiguration());
                mainOutput = fs.create(Paths.OUTPUT.get("baidu"));
                otherOut = fs.create(Paths.OUTPUT.get("other"));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

        @Override
        public void write(Text key, NullWritable value) throws IOException, InterruptedException {
            String log = key.toString();
            if (log.contains("baidu")) {
                mainOutput.writeBytes(log + "\n");
            } else {
                otherOut.writeBytes(log + "\n");
            }
        }

        @Override
        public void close(TaskAttemptContext context) {
            IOUtils.closeStream(mainOutput);
            IOUtils.closeStream(otherOut);
        }
    }

    @Override
    public RecordWriter<Text, NullWritable> getRecordWriter(TaskAttemptContext job) {
        return new LogRecordWriter(job);
    }
}


