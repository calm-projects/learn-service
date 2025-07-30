package com.atguigu.core.rdd.builder

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Memory_Par {

    def main(args: Array[String]): Unit = {

        // TODO 准备环境
        val sparkConf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        sparkConf.set("spark.default.parallelism", "5")
        val sc = new SparkContext(sparkConf)

        // TODO 创建RDD
        /*
          TODO 内存分区
          RDD的并行度 & 分区
          makeRDD方法可以传递第二个参数，这个参数表示分区的数量
          如果不传递默认使用如下 defaultParallelism：
            scheduler.conf.getInt("spark.default.parallelism", totalCores) totalCores是我们 setMaster 的local配置如果是1则为1如果是*则是当前计算机最大core
          如果传递使用我们传递的分区数
         */
        //val rdd = sc.makeRDD(List(1,2,3,4),2)
        val rdd = sc.makeRDD(List(1,2,3,4))

        // 将处理的数据保存成分区文件
        rdd.saveAsTextFile("output")

        // TODO 关闭环境
        sc.stop()
    }
}
