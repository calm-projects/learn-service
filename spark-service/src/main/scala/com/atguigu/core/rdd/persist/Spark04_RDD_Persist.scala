package com.atguigu.core.rdd.persist

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark04_RDD_Persist {

  def main(args: Array[String]): Unit = {
    val sparConf = new SparkConf().setMaster("local").setAppName("Persist")
    val sc = new SparkContext(sparConf)
    sc.setCheckpointDir("data/checkpoint")

    val list = List("Hello Scala", "Hello Spark")

    val rdd = sc.makeRDD(list)

    val flatRDD = rdd.flatMap(item => {
      println("###########")
      item.split(" ")
    })
    // 验证下俩个checkpoint会不会覆盖，理论上是不会的
    // TODO 其实这里压根没有触发， 因为只有action算子才会触发操作，所以看下collect的源码就知道这里为什么没有执行了
    flatRDD.checkpoint()

    val mapRDD = flatRDD.map(word => {
      println("@@@@@@@@@@@@")
      (word, 1)
    })
    // checkpoint 需要落盘，需要指定检查点保存路径
    // 检查点路径保存的文件，当作业执行完毕后，不会被删除
    // 一般保存路径都是在分布式存储系统：HDFS
    // TODO checkpoint之前一定要执行cache不然会执行俩次 这个很坑啊,
    // checkpoint是会从新执行一遍依赖然后checkpoint的，所以必须添加一个cache，
    // 不然就会多执行一次那添加它会影响性能且延长执行时间毫无意义
    mapRDD.checkpoint()

    val reduceRDD: RDD[(String, Int)] = mapRDD.reduceByKey(_ + _)
    reduceRDD.collect().foreach(println)
    println("**************************************")
    val groupRDD = mapRDD.groupByKey()
    groupRDD.collect().foreach(println)

    println("==========================================")
    // 在这里使用第一个checkpoint看看会如何，
    // TODO 这里触发checkpoint了
    flatRDD.collect().foreach(println)
    // TODO 下面这个使用到了checkpoint
    flatRDD.collect().foreach(println)
    sc.stop()
  }
}
