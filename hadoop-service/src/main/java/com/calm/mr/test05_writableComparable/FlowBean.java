package com.calm.mr.test05_writableComparable;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@Data
@Accessors(chain = true)
public class FlowBean implements WritableComparable<FlowBean> {
    private long upFlow;
    private long downFlow;
    private long sumFlow;

    public FlowBean setSumFlow() {
        this.sumFlow = this.upFlow + this.downFlow;
        return this;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(upFlow);
        out.writeLong(downFlow);
        out.writeLong(sumFlow);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        upFlow = in.readLong();
        downFlow = in.readLong();
        sumFlow = in.readLong();
    }

    @Override
    public int compareTo(FlowBean o) {
        // 总流量的倒序排序
        if (this.sumFlow > o.sumFlow) {
            return -1;
        } else if (this.sumFlow < o.sumFlow) {
            return 1;
        } else {
            // 按照上行流量的正序排
            return Long.compare(this.upFlow, o.upFlow);
        }
    }
}
