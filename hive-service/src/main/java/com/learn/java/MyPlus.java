package com.learn.java;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.IntWritable;

/**
 * 自定义Hive UDF：实现两个整数的加法运算
 * 继承GenericUDF而非UDF，支持更灵活的类型检查和复杂数据结构
 */
@Slf4j
public class MyPlus extends GenericUDF {


    /**
     * 初始化方法：验证参数类型和数量，确定返回值类型
     * 该方法在Hive查询编译阶段调用，仅执行一次
     */
    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        if (arguments.length != 2) {
            throw new UDFArgumentLengthException("MyPlus函数需要且仅需要2个参数");
        }
        for (ObjectInspector argument : arguments) {
            if (!(argument instanceof PrimitiveObjectInspector)) {
                throw new UDFArgumentLengthException("参数必须是基本类型，实际类型为:" + argument.getTypeName());
            }
            PrimitiveCategory category = ((PrimitiveObjectInspector) argument).getPrimitiveCategory();
            if (PrimitiveCategory.INT != category) {
                throw new UDFArgumentLengthException("参数必须是INT类型，实际类型为:" + category);
            }
        }
        return PrimitiveObjectInspectorFactory.javaIntObjectInspector;
    }

    /**
     * 核心计算方法：处理输入参数并返回结果
     * 该方法在Hive查询执行阶段调用，对每一行数据执行一次
     */
    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        try {
            // 可以看下类型其实是IntWritable
            log.info(arguments[0].get().getClass().getTypeName());
            IntWritable a = (IntWritable) arguments[0].get();
            IntWritable b = (IntWritable) arguments[1].get();
            return (a == null || b == null) ? null : a.get() + b.get();
        } catch (Exception e) {
            throw new HiveException("MyPlus函数计算时出错: " + e.getMessage());
        }
    }


    @Override
    public String getDisplayString(String[] children) {
        return "MyPlus(" + children[0] + ", " + children[1] + ")";
    }
}