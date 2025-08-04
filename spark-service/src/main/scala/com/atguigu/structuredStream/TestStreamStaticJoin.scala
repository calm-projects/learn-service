package com.atguigu.structuredStream

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, to_timestamp, window}
import org.apache.spark.sql.streaming.OutputMode

object TestStreamStaticJoin {
  /**
   * 2025-08-04 11:05:01,tom
   * 2025-08-04 11:06:01,jack
   * 2025-08-04 11:06:01,rose
   *
   * @param args
   */
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local[3]")
      .appName("天官赐福")
      .config("spark.sql.shuffle.partitions", 1)
      .getOrCreate()
    import spark.implicits._
    val staticDf = List(("tom", "cat"), ("jack", "boy")).toDF("name", "sex")
    val lines = spark.readStream.format("socket").option("host", "hadoop03").option("port", 9999).load()
    val dataDf = lines.as[String].flatMap {
      line => {
        val words = line.split(",")
        List((words(0), words(1)))
      }
    }.toDF("ts", "name").withColumn("event_time", to_timestamp($"ts", "yyyy-MM-dd HH:mm:ss"))
    dataDf.printSchema()

    // stream 和 static stream join 也是可以设置watermark是不会报错的，但是不起作用，本身也不应该设置,如果开窗的话可以设置
    val streamDf = dataDf.withWatermark("event_time", "1 minute")
    // 引用字段三种方式 $"colName" 'colName col(colName)
    // 1. 如果没有join条件，则每条数据匹配staticDf里面的所有数据
    // streamDf.join(staticDf)
    // 2. 带有join条件的，这里写一个全参数的，可以简写
    // val joinDf = streamDf.join(staticDf, Seq("name"), "inner")
    // 3. 写一个left join，没区别和 inner join
    // val joinDf = streamDf.join(staticDf, Seq("name"), "left")
    // 4. 聚合后join
    val windowDF01 = streamDf.groupBy(window(col("event_time"), "10 minute", "5 minute"), $"name").count()
    println("windowDF是否是Stream:", windowDF01.isStreaming)
    val joinDf = windowDF01.join(staticDf)

    joinDf.writeStream
      .outputMode(OutputMode.Append())
      .format("console")
      .option("truncate", value = false)
      .start()
      .awaitTermination()
  }
}