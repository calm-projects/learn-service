package com.atguigu.core.rdd.operator.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_RDD_Operator_Transform_Test {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Operator")
    val sc = new SparkContext(sparkConf)

    // TODO 算子 - map
    val rdd = sc.textFile("data/input")
    rdd.saveAsTextFile("data/output2")


    // 长的字符串
    // 短的字符串
    val mapRDD: RDD[String] = rdd.map(
      line => {
        val datas: Array[String] = line.split(" ")
        datas(0)
      }
    )
    mapRDD.collect().foreach(println)

    val rdd2 = sc.wholeTextFiles("data/input/words.txt")
    rdd2.foreach(println(_))

    sc.stop()

  }
}
