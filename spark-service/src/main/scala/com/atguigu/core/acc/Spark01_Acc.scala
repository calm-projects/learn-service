package com.atguigu.core.acc

import org.apache.spark.{SparkConf, SparkContext}

object Spark01_Acc {

  def main(args: Array[String]): Unit = {

    val sparConf = new SparkConf().setMaster("local").setAppName("Acc")
    val sc = new SparkContext(sparConf)

    val rdd = sc.makeRDD(List(1, 2, 3, 4), 2)

    // reduce : 分区内计算，分区间计算
    //val i: Int = rdd.reduce(_+_)
    //println(i)
    // TODO 这样做为什么不对? 这里是代码在driver端执行还是在execute端执行的知识点，这里sum会传递到execute端执行，
    // execute 执行完后并没有将 sum传回driver做merge操作所以println("sum = " + sum) 永远是0
    var sum = 0
    rdd.foreach(
      num => {
        sum += num
        println(s"sum: $sum")
      }
    )
    println("sum = " + sum)

    sc.stop()

  }
}
