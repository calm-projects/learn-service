package com.calm.mr.test05_writableComparable;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class FlowMapper extends Mapper<LongWritable, Text, FlowBean, Text> {

    private final FlowBean outK = new FlowBean();
    private final Text outV = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] split = value.toString().split("\t");
        outV.set(split[1]);
        outK.setUpFlow(Long.parseLong(split[split.length - 3]))
                .setDownFlow(Long.parseLong(split[split.length - 2]))
                .setSumFlow();
        context.write(outK, outV);
    }
}
