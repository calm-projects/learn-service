package com.wisers.sql

import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

abstract class UnitSpecSparkSql extends AnyFunSuite with BeforeAndAfterAll{
  /**
   * 参数参考地址 https://spark.apache.org/docs/2.4.1/submitting-applications.html#master-urls
   *    local：启动一个core
   *    local[*]: 使用与本地计算机相同的core
   *    local[k]: 使用K个工作线程在本地运行Spark(理想情况下，将其设置为您机器上的内核数)。
   *    spark://HOST:PORT 连接Spark standalone cluster master.端口默认7077
   *    yarn 连接yarn集群 根据 --deploy-mode 为client 还是 cluster 决定driver运行在本地还是运行在yarn上 默认为client
   *
   *    端口信息：
   *      Spark查看当前Spark-shell运行任务情况端口号：4040（计算）
   *      Spark Master内部通信服务端口号：7077
   *      Standalone模式下，Spark Master Web端口号：8080（资源）
   *      Spark历史服务器端口号：18080
   *      Hadoop YARN任务运行情况查看端口号：8088
   */

    // 使用delta暂时用下面的
  /*
  val spark = SparkSession
    .builder()
    .appName("天官赐福")
    .master("local[*]")
    .config("spark.sql.files.ignoreMissingFiles", true)
    .config("spark.databricks.delta.retentionDurationCheck.enabled", false)
    // 不将hive配置文件添加到项目或者spark集群或者未配置config hive.metastore.uris spark.sql.warehouse.dir 默认会使用spark的内置hive
    // 一般用来做测试，如果想要使用外部的hive，需要我们进行上述的任一一个配置
    // spark on hive 指的是spark可以对hive进行读写
    // hive on spark 是指 hive的引擎不用mr了也不用tez用spark
    .config("hive.metastore.uris", "thrift://localhost:9083")
    .config("spark.sql.warehouse.dir", "hdfs://localhost:9000/user/hive/warehouse")
//    .enableHiveSupport()
    .getOrCreate()
  **/

  val spark = SparkSession
    .builder()
    .appName("天官赐福")
    .master("local[*]")
    .config("spark.sql.files.ignoreMissingFiles", true)
    .config("spark.databricks.delta.retentionDurationCheck.enabled", false)
    .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
    .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
    .getOrCreate()

  import spark.implicits._

  override protected def afterAll(): Unit = {
    println("===================================================================")
    spark.stop()
  }
}
