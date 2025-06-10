package com.atguigu.structuredStream

import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.{DataFrame, SparkSession}

object C10_KafkaSink {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[1]")
      .appName("Test")
      .getOrCreate()
    import spark.implicits._

    val lines: DataFrame = spark.readStream
      .format("socket") // 设置数据源
      .option("host", "localhost")
      .option("port", 9999)
      .load

    val words = lines.as[String]
      .flatMap(_.split("\\W+"))
      .groupBy("value")
      .count()
      .map(row => row.getString(0) + "," + row.getLong(1))
      .toDF("value") // 写入数据时候, 必须有一列 "value"

    words.writeStream
      .outputMode(OutputMode.Update()) // 将状态都存在了ckeckpoint里面了,checkpoint 里面生成大量的小文件
      .format("kafka")
      .trigger(Trigger.ProcessingTime(0))
      .option("kafka.bootstrap.servers", "hadoop01:9092,hadoop02:9092,hadoop03:9092") // kafka 配置
      .option("topic", "test") // kafka 主题
      .option("checkpointLocation", "datas/atGuiGu/StreamStruct/ck3") // 必须指定 checkpoint 目录
      .start
      .awaitTermination()
  }
}
