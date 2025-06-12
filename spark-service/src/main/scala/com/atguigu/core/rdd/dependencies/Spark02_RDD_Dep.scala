package com.atguigu.core.rdd.dependencies

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark02_RDD_Dep {

  def main(args: Array[String]): Unit = {
    val sparConf = new SparkConf().setMaster("local").setAppName("Dep")
    val sc = new SparkContext(sparConf)

    val lines: RDD[String] = sc.textFile("data/spark/words.txt", 2)
    println(lines.dependencies)
    println("*************************")
    val words: RDD[String] = lines.flatMap(_.split(" "))
    println(words.dependencies)
    println("*************************")
    println(words.getNumPartitions)
    // 这个是窄依赖，关键看是否发生了shuffle
    val coalesceToOne = words.coalesce(1)
    println(coalesceToOne.dependencies)
    println("*************************")
    println(coalesceToOne.getNumPartitions)
    val wordToOne = words.map(word => (word, 1))
    println(wordToOne.dependencies)
    println("*************************")
    val wordToSum: RDD[(String, Int)] = wordToOne.reduceByKey(_ + _)
    println(wordToSum.dependencies)
    println("*************************")
    val array: Array[(String, Int)] = wordToSum.collect()
    array.foreach(println)

    sc.stop()

  }
}
