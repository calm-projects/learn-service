package com.wisers.core

import com.wisers.UnitSpec
import org.apache.spark.rdd.RDD

/**
 * spark action 算子
 * spark 一个action 一个 job
 */
class TestAction extends UnitSpec{

  /**
   * reduce 先聚合分区内数据，再聚合分区间数据
   */
  test("test reduce") {
    val dataRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4))
    val value1: Int = dataRdd.reduce((v1, v2) => v1 + v2)
    val value2: Int = dataRdd.reduce(_ + _)
    // 10
    println(value1)
    // 10
    println(value2)
  }

  /**
   * collect 在驱动程序（Driver）中，以数组Array的形式返回数据集的所有元素
   */
  test("test collect") {
    val dataRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4))
    val ints: Array[Int] = dataRdd.collect()
    // 1 2 3 4
    ints.foreach(println)
  }

  /**
   * count 返回RDD中元素的个数
   */
  test("test count") {
    val dataRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4))
    val cnt: Long = dataRdd.count()
    // count: 4
    println(s"count: $cnt")
  }

  /**
   * first 返回RDD中的第一个元素
   */
  test("test first") {
    val dataRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4))
    val first: Int = dataRdd.first()
    // first: 1
    println(s"first: $first")
  }

  /**
   * task 返回一个由RDD的前n个元素组成的数组
   */
  test("test task") {
    val dataRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4))
    val take: Array[Int] = dataRdd.take(2)
    // take: [I@256aa5f2
    println(s"take: $take")
  }

  /**
   * takeOrdered 返回该RDD排序后的前n个元素组成的数组
   */
  test("test takeOrdered") {
    val dataRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4))
    val takeOrdered: Array[Int] = dataRdd.takeOrdered(2)
    // takeOrdered: [I@7a231dfd
    println(s"takeOrdered: $takeOrdered")
  }

  /**
   * aggregate 分区的数据通过初始值和分区内的数据进行聚合，然后再和初始值进行分区间的数据聚合
   *  *** 分区间计算的时候也加一次初始值 莫名其妙
   */
  test("test aggregate") {
    val dataRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4))
    val aggregate: Int = dataRdd.aggregate(2)(_ + _, _ + _)
    println(dataRdd.getNumPartitions)
    // aggregate: 44= 16*2 +10 +2
    println(s"aggregate: $aggregate")
  }

  /**
   * fold aggregate的简化版操作 分区间和分区内操作相同
   */
  test("test fold") {
    val dataRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4))
    val fold: Int = dataRdd.fold(2)(_ + _)
    // fold: 44
    println(s"fold: $fold")
  }

  /**
   * countByKey 统计每种key的个数
   */
  test("test countByKey") {
    val dataRdd: RDD[(String, Int)] = spark.makeRDD(List(("a", 1), ("b", 1)))
    val countByKey: collection.Map[String, Long] = dataRdd.countByKey()
    // countByKey: Map(a -> 1, b -> 1)
    println(s"countByKey: $countByKey")
  }

  /**
   * save
   *  saveAsTextFile
   *  saveAsObjectFile
   *  saveAsSequenceFile
   */
  test("test save") {
    val dataRdd: RDD[(String, Int)] = spark.makeRDD(List(("a", 1), ("b", 1)))
    val path: String = "spark-demo/src/main/scala/com/wisers/output/"
    dataRdd.saveAsTextFile(path + "file1")
    dataRdd.saveAsObjectFile(path + "file2")
    dataRdd.saveAsSequenceFile(path + "file3")
  }


  /**
   * foreach
   *  分布式遍历RDD中的每一个元素，调用指定函数
   */
  test("test foreach") {
    val dataRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4))
    dataRdd.foreach(println)
  }
}
