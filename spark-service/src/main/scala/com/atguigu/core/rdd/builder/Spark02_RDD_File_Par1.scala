package com.atguigu.core.rdd.builder

import org.apache.spark.{SparkConf, SparkContext}

object Spark02_RDD_File_Par1 {

  def main(args: Array[String]): Unit = {

    // TODO 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("RDD")
    val sc = new SparkContext(sparkConf)

    // TODO 创建RDD
    /*
        TODO 看下数据分配规则
         1. 数据以行为单位进行读取
            spark读取文件，采用的是hadoop的方式读取，所以一行一行读取，和字节数没有关系
         2. 数据读取时以偏移量为单位,偏移量不会被重复读取
         比如我们文件内容为：
           1@@   => 012
           2@@   => 345
           3     => 6
         设置2个分区，7/2=3 7/3=2...1（1.1） 也就是为3个分区 一个分区是3个字节
            下面的【0-3】都是闭区间，而且偏移量不会重复读取，而且是正行读取
         【0分区】 --- 【0-3】---》读取的数据为 1 2
         【1分区】 --- 【3-6】---》读取的数据为 3
         【2分区】 --- 【6-7】---》读取的数据为 啥也不读
         如果数据是 1234567呢？
         【0分区】 --- 【0-3】---》因为是整行读取所以是 1234567
         【1分区】 --- 【3-6】---》读取的数据为 啥也不读
         【2分区】 --- 【6-7】---》读取的数据为 啥也不读
     */

    val rdd = sc.textFile("datas/1.txt", 2)

    rdd.saveAsTextFile("output1")

    // TODO 关闭环境
    sc.stop()
  }
}
