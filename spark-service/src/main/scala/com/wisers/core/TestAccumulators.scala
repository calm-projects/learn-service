package com.wisers.core

import com.wisers.UnitSpec
import org.apache.spark.rdd.RDD

/**
 * 描述信息
 */
class TestAccumulators extends UnitSpec{

  /**
   * 累加器用来把Executor端变量信息聚合到Driver端。在Driver程序中定义的变量，
   * 在Executor端的每个Task都会得到这个变量的一份新的副本，每个task更新这些副本的值后，传回Driver端进行merge。
   */
  test("test longAccumulator") {
    val dataRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4, 5))
    val cusACC = spark.longAccumulator("cusACC");
    dataRdd.foreach(cusACC.add(_))
    var sum: Int = 0;
    dataRdd.foreach(sum+=_)
    // sum = 0 foreach不是在driver端执行的
    println("sum = " + sum)
    // cusACC = 15
    println("cusACC = " + cusACC.value)
  }

}
