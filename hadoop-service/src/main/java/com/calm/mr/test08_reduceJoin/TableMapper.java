package com.calm.mr.test08_reduceJoin;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

public class TableMapper extends Mapper<LongWritable, Text, Text, TableBean> {

    private String fileName;
    private final Text outK = new Text();
    private final TableBean outV = new TableBean();

    @Override
    protected void setup(Context context) {
        FileSplit split = (FileSplit) context.getInputSplit();
        fileName = split.getPath().getName();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String[] split = line.split("\t");
        if (fileName.contains("order")) {
            // id pid amount
            outK.set(split[1]);
            outV.setId(split[0]).setPid(split[1]).setAmount(Integer.parseInt(split[2])).setPname("").setFlag("order");
        } else {
            // pid pname
            outK.set(split[0]);
            outV.setId("").setPid(split[0]).setAmount(0).setPname(split[1]).setFlag("pd");
        }
        context.write(outK, outV);
    }
}
