package com.atguigu.core.rdd.operator.transform

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark01_map {

    def main(args: Array[String]): Unit = {

        val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Operator")
        val sc = new SparkContext(sparkConf)

        // TODO 算子 - map
        val rdd = sc.parallelize(List(1, 2, 3, 4))
        println(rdd.getNumPartitions)
        val rdd1 = sc.makeRDD(List(1,2,3,4))
        println(rdd1.getNumPartitions )

        // 1,2,3,4
        // 2,4,6,8

        // 转换函数
        def mapFunction(num:Int): Int = {
            num * 2
        }

        //val mapRDD: RDD[Int] = rdd.map(mapFunction)
        //val mapRDD: RDD[Int] = rdd.map((num:Int)=>{num*2})
        //val mapRDD: RDD[Int] = rdd.map((num:Int)=>num*2)
        //val mapRDD: RDD[Int] = rdd.map((num)=>num*2)
        //val mapRDD: RDD[Int] = rdd.map(num=>num*2)
        val mapRDD: RDD[Int] = rdd.map(_*2)

        mapRDD.collect().foreach(println)

        sc.stop()

    }
}
