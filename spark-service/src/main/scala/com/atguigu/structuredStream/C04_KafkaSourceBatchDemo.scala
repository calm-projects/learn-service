package com.atguigu.structuredStream

import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{DataFrame, SparkSession}

object C04_KafkaSourceBatchDemo {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("KafkaSourceDemo")
      .getOrCreate()

    import spark.implicits._

    // 得到的 df 的 schema 是固定的: key,value,topic,partition,offset,timestamp,timestampType
    val lines = spark.read
      .format("kafka") // 设置 kafka 数据源
      .option("kafka.bootstrap.servers", "hadoop01:9092,hadoop02:9092,hadoop03:9092")
      .option("subscribe", "test") // 也可以订阅多个主题:   "topic1,topic2"
      .option("startingOffsets", "earliest")
      .option("endingOffsets", "latest")
      .load
      .selectExpr("CAST(value AS STRING)")
      .as[String]


    val query: DataFrame = lines.flatMap(_.split("\\W+")).groupBy("value").count()

    query.write // 使用 write 而不是 writeStream
      .format("console")
      .save()
  }
}