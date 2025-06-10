package com.atguigu.structuredStream

import org.apache.spark.sql.functions.expr
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.sql.Timestamp

/**
 * 描述信息
 *
 * @create: 2023-03-26 01:02
 */
object C08_LeftJoinStreamAndStream03 {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("StreamStream1")
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
      }).toDF("name1", "sex", "ts1")
      .withWatermark("ts1", "2 minutes")

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
      }).toDF("name2", "age", "ts2")
      .withWatermark("ts2", "1 minutes")


    // join 操作 右表必须设置watermark 必须设置time条件
    val joinResult1: DataFrame = nameSexStream.join(nameAgeStream, $"name1" === $"name2", "left_outer")
    val joinResult: DataFrame = nameSexStream.join(
      nameAgeStream,
      expr(
        """
          |name1=name2 and
          |ts2 >= ts1 and
          |ts2 <= ts1 + interval 1 minutes
                  """.stripMargin), "left_outer")

    /**
     * 超过水位线就不再输出了
     * 实际是
     *  ts2 -ts1 <= 1 and >= 0
     */

    joinResult.writeStream
      .outputMode("append")
      .format("console")
      .trigger(Trigger.ProcessingTime(0))
      .start()
      .awaitTermination()
  }
}
