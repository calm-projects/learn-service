package com.atguigu.core.rdd.builder

import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Memory_Par1 {

    def main(args: Array[String]): Unit = {

        // TODO 准备环境
        val sparkConf = new SparkConf().setMaster("local[*]").setAppName("RDD")
        val sc = new SparkContext(sparkConf)

        // TODO 创建RDD
        /*
          TODO 内存数据分配源码
          看下数据是如何分配的，直接看下源码
          (0 until numSlices).iterator.map { i =>
               val start = ((i * length) / numSlices).toInt
               val end = (((i + 1) * length) / numSlices).toInt
               (start, end)
          }
         */

        // 【1，2】，【3，4】
        //val rdd = sc.makeRDD(List(1,2,3,4), 2)
        // 【1】，【2】，【3，4】
        //val rdd = sc.makeRDD(List(1,2,3,4), 3)
        // 【1】，【2,3】，【4,5】
        val rdd = sc.makeRDD(List(1,2,3,4,5), 3)

        // 将处理的数据保存成分区文件
        rdd.saveAsTextFile("output")

        // TODO 关闭环境
        sc.stop()
    }
}
