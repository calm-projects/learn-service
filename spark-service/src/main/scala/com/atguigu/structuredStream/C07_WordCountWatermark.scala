package com.atguigu.structuredStream

import java.sql.Timestamp
import org.apache.spark.sql._
import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery, Trigger}
object C07_WordCountWatermark {
  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("WordCountWatermark1")
      .getOrCreate()

    import spark.implicits._
    val lines: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9999)
      .load
    // 输入的数据中包含时间戳, 而不是自动添加的时间戳
    val words: DataFrame = lines.as[String].flatMap(line => {
      val split: Array[String] = line.split(",")
      val tuples: Array[(String, Timestamp)] = split(1).split(" ").map((_, Timestamp.valueOf(split(0))))
      tuples
    }).toDF("word", "timestamp")

    import org.apache.spark.sql.functions._
    val wordCounts: Dataset[Row] = words
      // 添加watermark, 参数 1: event-time 所在列的列名 参数 2: 延迟时间的上限.
      .withWatermark("timestamp", "2 minutes")
      .groupBy(window($"timestamp", "10 minutes", "2 minutes"), $"word")
      .count()

    /**
     * 在watermark下
     * complete不变 watermark 对它无作用还是会缓存所有结果 建议不要使用
     * update：还是只更新更改的值
     * append：可以进行聚合了，但是append只有窗口结束的时候才会输出这也符合它的定位 输出不能变更的数据
     */
    val query: StreamingQuery = wordCounts.writeStream
      .outputMode(OutputMode.Append())
      .trigger(Trigger.ProcessingTime(3000))
      .format("console")
      .option("truncate", "false")
      .start
    query.awaitTermination()
  }
}