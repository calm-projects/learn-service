package com.atguigu.core.rdd.operator.transform

import org.apache.spark.{SparkConf, SparkContext}

object Spark08_RDD_Operator_Transform {

    def main(args: Array[String]): Unit = {

        val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Operator")
        val sc = new SparkContext(sparkConf)

        // TODO 算子 - filter
        val rdd = sc.makeRDD(List(1,2,3,4,5,6,7,8,9,10), 2)

        // sample算子需要传递三个参数
        // 1. 第一个参数表示，抽取数据后是否将数据返回 true（放回），false（丢弃）
        // 2. 第二个参数表示，
        //         如果抽取不放回的场合：数据源中每条数据被抽取的概率，基准值的概念
        //         如果抽取放回的场合：表示数据源中的每条数据被抽取的可能次数
        // 3. 第三个参数表示，抽取数据时随机算法的种子
        //                    如果不传递第三个参数，那么使用的是当前系统时间

        /*
            withReplacement: 是否有放回地采样（true 表示一个元素可能被采样多次）
            fraction: 采样比例
                无放回：是期望的比例（期望采样出原来数据量的 fraction 倍）
                有放回：是每个元素被采样的期望次数
            seed: 随机种子，保证可复现性
         */
//        println(rdd.sample(
//            false,
//            0.4
//            //1
//        ).collect().mkString(","))

        println(rdd.sample(
            true,
            0.4
            //1
        ).collect().mkString(","))


        sc.stop()

    }
}
