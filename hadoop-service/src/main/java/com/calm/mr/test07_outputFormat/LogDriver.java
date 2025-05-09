package com.calm.mr.test07_outputFormat;

import com.calm.data.Paths;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class LogDriver {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);

        job.setJarByClass(LogDriver.class);
        job.setMapperClass(LogMapper.class);
        job.setReducerClass(LogReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        //设置自定义的outputFormat
        job.setOutputFormatClass(LogOutputFormat.class);

        FileInputFormat.setInputPaths(job, Paths.LOG.get());
        //虽然我们自定义了outputFormat，但是因为我们的outputFormat继承自FileOutputFormat
        //而FileOutputFormat要输出一个_SUCCESS文件，所以在这还得指定一个输出目录
        FileOutputFormat.setOutputPath(job, Paths.OUTPUT.get("success"));

        boolean b = job.waitForCompletion(true);
        System.exit(b ? 0 : 1);

    }
}