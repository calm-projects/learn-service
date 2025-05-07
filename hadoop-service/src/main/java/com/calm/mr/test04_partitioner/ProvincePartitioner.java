package com.calm.mr.test04_partitioner;

import com.calm.mr.test02_writable.FlowBean;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class ProvincePartitioner extends Partitioner<Text, FlowBean> {
    @Override
    public int getPartition(Text text, FlowBean flowBean, int numPartitions) {
        String prePhone = text.toString().substring(0, 3);
        int partition;
        switch (prePhone) {
            case "136":
                partition = 0;
                break;
            case "137":
                partition = 1;
                break;
            case "138":
                partition = 2;
                break;
            case "139":
                partition = 3;
                break;
            default:
                partition = 4;
                break;
        }
        return partition;
    }
}
