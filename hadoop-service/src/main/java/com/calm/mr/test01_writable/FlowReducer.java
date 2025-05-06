package com.calm.mr.test01_writable;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class FlowReducer extends Reducer<Text, FlowBean, Text, FlowBean> {
    private final FlowBean outV = new FlowBean();

    @Override
    protected void reduce(Text key, Iterable<FlowBean> values, Context context) throws IOException, InterruptedException {
        long totalUp = 0;
        long totaldown = 0;
        for (FlowBean value : values) {
            totalUp += value.getUpFlow();
            totaldown += value.getDownFlow();
        }

        outV.setDownFlow(totaldown).setUpFlow(totalUp).setSumFlow();

        context.write(key, outV);
    }
}
