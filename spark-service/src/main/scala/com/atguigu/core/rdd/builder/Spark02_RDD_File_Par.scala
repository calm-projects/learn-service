package com.atguigu.core.rdd.builder

import org.apache.spark.{SparkConf, SparkContext}

object Spark02_RDD_File_Par {

    def main(args: Array[String]): Unit = {

        // TODO 准备环境
        val sparkConf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(sparkConf)

        // TODO 创建RDD
        /*
        TODO 文件分区
         textFile可以将文件作为数据处理的数据源，默认也可以设定分区。
            minPartitions : 最小分区数量,这里是最小分区数，不是分区数，默认分区规则和内存是一样的。
            math.min(defaultParallelism, 2)
            如果不想使用默认的分区数量，可以通过第二个参数指定分区数
            Spark读取文件，底层其实使用的就是Hadoop的读取方式
            分区数计算规则如下：
                内容总字节：totalSize =7
                每个分区的目标字节（比如2个分区）：goalSize：7/2 = 3 (byte)
                分区数： numPartition = 7/3 = 2 ... 1（hadoop切分文件的时候有一个1.1倍也就是10%，1/2=50%，超过了就新创建一个分区，所以是3个分区）
         */
        val rdd = sc.textFile("datas/1.txt", 2)

        rdd.saveAsTextFile("output")


        // TODO 关闭环境
        sc.stop()
    }
}
