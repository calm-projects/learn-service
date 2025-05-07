package com.calm.mr.test08_reduceJoin;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
public class TableReducer extends Reducer<Text, TableBean, TableBean, NullWritable> {

    @Override
    protected void reduce(Text key, Iterable<TableBean> values, Context context) throws IOException, InterruptedException {
        ArrayList<TableBean> orderBeans = new ArrayList<>();
        TableBean pdBean = new TableBean();
        for (TableBean value : values) {
            if ("order".equals(value.getFlag())) {
                TableBean entity = new TableBean().setId(value.getId()).setPid(value.getPid())
                        .setPname(value.getPname()).setAmount(value.getAmount()).setFlag(value.getFlag());
                orderBeans.add(entity);
            } else {
                pdBean.setId(value.getId()).setPid(value.getPid())
                        .setPname(value.getPname()).setAmount(value.getAmount()).setFlag(value.getFlag());
            }
        }
        for (TableBean orderBean : orderBeans) {
            orderBean.setPname(pdBean.getPname());
            context.write(orderBean, NullWritable.get());
        }
    }
}
