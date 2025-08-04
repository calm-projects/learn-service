package com.atguigu.structuredStream

import org.apache.spark.sql.functions.{expr, to_timestamp}
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{DataFrame, SparkSession}

object TestStreamStreamJoin {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local[3]")
      .appName("天官赐福")
      .config("spark.sql.shuffle.partitions", 1)
      .getOrCreate()
    import spark.implicits._

    val streamDf01: DataFrame = spark.readStream.format("socket")
      .option("host", "hadoop03").option("port", 9999).load()
      .as[String].flatMap {
        line => {
          val words = line.split(",")
          List((words(0), words(1)))
        }
      }.toDF("ts1", "name")
      .withColumn("event_time", to_timestamp($"ts1", "yyyy-MM-dd HH:mm:ss"))
      .withWatermark("event_time", "1 minute")


    val streamDf02: DataFrame = spark.readStream.format("socket")
      .option("host", "hadoop03").option("port", 9998).load()
      .as[String].flatMap {
        line => {
          val words = line.split(",")
          List((words(0), words(1)))
        }
      }.toDF("ts2", "name")
      .withColumn("event_time", to_timestamp($"ts2", "yyyy-MM-dd HH:mm:ss"))
      .withWatermark("event_time", "5 minute")


    /*
      1. 没有条件直接报错
      val joinDf = streamDf01.join(streamDf02)
     */

    /*
      2. 尚未配置watermark，inner join 存储状态的，一般禁止使用，状态太大
      2025-08-04 11:05:01,tom
      val joinDf = streamDf01.join(streamDf02, Seq("name"))
     */

    /*
      3. 设置水位线，但是不设置 join约束范围
      2025-08-04 11:05:01,tom
      2025-08-04 11:06:01,jack
      01 02 输入 01的watermark不是 1min嘛，其实还是全部匹配说明，如果在join不设置watermark的话，在stream上设置是不起作用的
      2025-08-04 11:10:01,jack
      val joinDf = streamDf01.join(streamDf02, Seq("name"))
     */

    /*
      3. 设置水位线，设置 join约束范围
      2025-08-04 11:05:01,tom
      2025-08-04 11:06:01,jack
      02 输入 这时候 01 不会join 因为 01和02存在join条件，02必须是在01间隔2min之内
      2025-08-04 11:13:01,jack
      02 输入 不会join
      2025-08-04 11:07:01,jack
      02 输入 不会join 当前最低的watermark 是 2025-08-04 11:06:01 -1min = 2025-08-04 11:05:01
      2025-08-04 11:05:01,jack
      02 输入 我们验证下join是按照2个当前stream的最低watermark清除的状态
      2025-08-04 11:13:01,jack
      02 输入 这时候在输入，这条数据已经低于02的watermark了 2025-08-04 11:13:01-5=2025-08-04 11:8:01，但是依旧join 说明是按照01的watermark清除的状态信息
      2025-08-04 11:05:03,jack

      outer join 必须指定至少一个watermark，逻辑一样的，这里不进行测试了
     */
    val joinDf = streamDf01.as("t1").join(streamDf02.as("t2"),
      expr(
        """
            t1.name = t2.name AND
            t1.event_time >= t2.event_time AND
            t1.event_time <= t2.event_time + interval 2 minute
        """)
    )

    /*
      val windowDF01 = streamDf01.groupBy(window(col("event_time"), "10 minute", "5 minute"), $"name").count()
      println("windowDF是否是Stream:", windowDF01.isStreaming)
      val windowDF02 = streamDf02.groupBy(window(col("event_time"), "10 minute", "2 minute"), $"name").count()
      println("windowDF是否是Stream:", windowDF02.isStreaming)

      报错：Multiple streaming aggregations are not supported with streaming DataFrames/Datasets
      val joinDf = windowDF01.as("t1").join(windowDF02.as("t2"), expr(
        """
              t1.name = t2.name AND
              t1.window.start = t2.window.start AND
              t1.window.end = t2.window.end
          """))
     */

    joinDf.writeStream
      .outputMode(OutputMode.Append())
      .format("console")
      .option("truncate", value = false)
      .start()
      .awaitTermination()
  }
}