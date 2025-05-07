package com.calm.mr.test02_writable;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


@Data
@Accessors(chain = true)
public class FlowBean implements Writable {
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

}
