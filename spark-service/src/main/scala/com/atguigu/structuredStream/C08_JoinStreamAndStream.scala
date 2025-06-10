package com.atguigu.structuredStream

import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.sql.Timestamp

/**
 * 描述信息
 *
 * @create: 2023-03-26 01:02
 */
object C08_JoinStreamAndStream {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("StreamingStatic")
      .getOrCreate()
    import spark.implicits._

    // 第 1 个 stream
    val nameSexStream: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9998)
      .load
      .as[String]
      .map(line => {
        val arr: Array[String] = line.split(",")
        (arr(0), arr(1), Timestamp.valueOf(arr(2)))
      }).toDF("name", "sex", "ts1")

    // 第 2 个 stream
    val nameAgeStream: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9999)
      .load
      .as[String]
      .map(line => {
        val arr: Array[String] = line.split(",")
        (arr(0), arr(1).toInt, Timestamp.valueOf(arr(2)))
      }).toDF("name", "age", "ts2")


    // join 操作
    val joinResult: DataFrame = nameSexStream.join(nameAgeStream, "name")

    joinResult.writeStream
      .outputMode("append")
      .format("console")
      .trigger(Trigger.ProcessingTime(0))
      .start()
      .awaitTermination()
  }
}
