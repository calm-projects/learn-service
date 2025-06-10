package com.atguigu.sourceAnalysis;

/**
 * 描述信息
 *
 * @create: 2023-03-23 21:31
 */
public class HadoopFIle {
    // 1。0 hadoopFile怎么切割的
    /**
     * 1.0 内部创建的是hadoopFile，使用的是 TextInputFormat
     *   def textFile(
     *       path: String,
     *       minPartitions: Int = defaultMinPartitions): RDD[String] = withScope {
     *     assertNotStopped()
     *     hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text],
     *       minPartitions).map(pair => pair._2.toString).setName(path)
     *   }
     *
     * 2.0 打开 TextInputFormat 的父类 FileInputFormat 看方法 getSplits，看几个关键点就好了
     * long goalSize = totalSize / (numSplits == 0 ? 1 : numSplits);
     * long minSize = Math.max(job.getLong(org.apache.hadoop.mapreduce.lib.input.
     *       FileInputFormat.SPLIT_MINSIZE, 1), minSplitSize);
     * Math.max(minSize, Math.min(goalSize, blockSize));
     */

}
