package com.atguigu.structuredStream

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, to_timestamp, window}
import org.apache.spark.sql.streaming.OutputMode

object TestWindow {
  /**
   * 2025-08-04 11:05:01,tom
   * 2025-08-04 11:06:01,tom
   * 2025-08-04 11:10:01,tom
   * 2025-08-04 11:11:01,tom
   * 2025-08-04 11:12:01,tom
   * 2025-08-04 11:13:01,tom
   * 2025-08-04 11:15:01,tom
   * 2025-08-04 11:30:01,tom
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

    val lines = spark.readStream.format("socket").option("host", "hadoop03").option("port", 9999).load()
    val dataDf = lines.as[String].flatMap {
      line => {
        val words = line.split(",")
        List((words(0), words(1)))
      }
    }.toDF("ts", "word").withColumn("event_time", to_timestamp($"ts", "yyyy-MM-dd HH:mm:ss"))
    dataDf.printSchema()

    val dataDfWithWt = dataDf.withWatermark("event_time", "1 minute")
    // 引用字段三种方式 $"colName" 'colName col(colName)
    val countDf = dataDfWithWt.groupBy(window('event_time, "5 minute", "3 minute"), col("word")).count()

    countDf.writeStream
      .outputMode(OutputMode.Update())
      .format("console")
      .option("truncate", value = false)
      .start()
      .awaitTermination()
  }
}