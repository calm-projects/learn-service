package com.wisers

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

abstract class UnitSpec extends AnyFunSuite with BeforeAndAfterAll{
  /**
   * 参数参考地址 https://spark.apache.org/docs/2.4.1/submitting-applications.html#master-urls
   *    local：启动一个core
   *    local[*]: 使用与本地计算机相同的core
   *    local[k]: 使用K个工作线程在本地运行Spark(理想情况下，将其设置为您机器上的内核数)。
   *    spark://HOST:PORT 连接Spark standalone cluster master.端口默认7077
   *    yarn 连接yarn集群 根据 --deploy-mode 为client 还是 cluster 决定driver运行在本地还是运行在yarn上 默认为client
   *
   *
   *    端口信息：
   *      Spark查看当前Spark-shell运行任务情况端口号：4040（计算）
   *      Spark Master内部通信服务端口号：7077
   *      Standalone模式下，Spark Master Web端口号：8080（资源）
   *      Spark历史服务器端口号：18080
   *      Hadoop YARN任务运行情况查看端口号：8088
   */

  val conf: SparkConf = new SparkConf().setAppName("spark test").setMaster("local[*]")
  val spark: SparkContext = new SparkContext(conf)


  override protected def afterAll(): Unit = {
    println("===================================================================")
    spark.stop()
  }
}
