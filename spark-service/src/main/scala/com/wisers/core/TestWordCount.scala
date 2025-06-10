package com.wisers.core

import com.wisers.UnitSpec
import org.apache.spark.rdd.RDD

/**
 * spark wordCount 入门
 */
class TestWordCount extends UnitSpec{

  test("test wordCount") {
    val path: String = "spark-demo/src/main/scala/com/wisers/input/wordCount"
    /*
    val rdd: RDD[String] = spark.textFile(path)
    val flatRdd: RDD[String] = rdd.flatMap(_.split(" "))
    val mapRdd: RDD[(String, Int)] = flatRdd.map((_, 1))
    val foldRDD: RDD[(String, Int)] = mapRdd.foldByKey(0)(_ + _)
    foldRDD.collect().foreach(println)
    */
    spark.textFile(path).flatMap(_.split(" ")).map((_, 1)).reduceByKey(_ + _).collect().foreach(println)
  }

}
