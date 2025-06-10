package com.wisers.core

import com.wisers.UnitSpec
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

/**
 * 描述信息
 */
class TestBroadcast extends UnitSpec{

  /**
   * 广播变量用来高效分发较大的对象。向所有工作节点发送一个较大的只读值，以供一个或多个Spark操作使用。
   */
  test("test longAccumulator") {
    val dataRdd = spark.makeRDD(List( ("a",1), ("b", 2), ("c", 3), ("d", 4) ),4)
    val lst = List( ("a",4), ("b", 5), ("c", 6), ("d", 7) )
    val broadcast: Broadcast[List[(String, Int)]] = spark.broadcast(lst)
    val value: RDD[(String, (Int, Int))] = dataRdd.map {
      case (k, v) =>
        var vTmp = 0
        for((k1, v2) <- broadcast.value if k == k1) {
          vTmp = v2
        }
        (k, (v, vTmp))
    }
    value.collect().foreach(println)
  }

}
