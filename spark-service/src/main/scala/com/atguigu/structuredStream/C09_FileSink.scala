package com.atguigu.structuredStream

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object C09_FileSink {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[1]")
      .appName("Test")
      .getOrCreate()
    import spark.implicits._

//    val lines: DataFrame = spark.readStream
//      .format("socket") // 设置数据源
//      .option("host", "localhost")
//      .option("port", 9999)
//      .load

    val df: DataFrame = spark.readStream
      .format("kafka") // 设置 kafka 数据源
      .option("kafka.bootstrap.servers", "hadoop01:9092,hadoop02:9092,hadoop03:9092")
      .option("subscribe", "test") // 也可以订阅多个主题:   "topic1,topic2"
      .option("startingOffsets", "earliest")
      .load

    val lines = df.selectExpr("CAST(value AS STRING)")
      .as[String]


    val words: DataFrame = lines.as[String].flatMap(line => {
      line.split("\\W+").map(word => {
        (word, word.reverse)
      })
    }).toDF("原单词", "反转单词")

    /**
     * 文件sink根本不能用，会生成大量小文件
     */

    words.writeStream
      .outputMode("append")
      .format("json") //  // 支持 "orc", "json", "csv"，"parquet"
      .option("path", "datas/atGuiGu/StreamStruct/output/") // 输出目录
      .option("checkpointLocation", "datas/atGuiGu/StreamStruct/ck1")  // 必须指定 checkpoint 目录
      .start
      .awaitTermination()
  }
}
