package com.atguigu.structuredStream

import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery, Trigger}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
 * 描述信息
 *
 * @create: 2023-03-25 22:50
 */
object C01_WordCount {
  def main(args: Array[String]): Unit = {
    // 1. 创建 SparkSession. 因为 ss 是基于 spark sql 引擎, 所以需要先创建 SparkSession
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("WordCount1")
      .getOrCreate()
    import spark.implicits._

    // 2. 从数据源(socket)中加载数据.
    val lines: DataFrame = spark.readStream
      .format("socket") // 设置数据源
      .option("host", "localhost")
      .option("port", 9999)
      .load

    // 3. 把每行数据切割成单词
    val words: Dataset[String] = lines.as[String].flatMap(_.split("\\W"))

    // 4. 计算 word count
    val wordCounts: DataFrame = words.groupBy("value").count()

    // 5. 启动查询, 把结果打印到控制台
    /**
     * Complete：完全输出
     * Append：只是输出追加的值不能进行更新，不能agg
     * Update ： 只会输出更新的值，如果不是agg和append一样
     */
    val query: StreamingQuery = wordCounts.writeStream
      .outputMode(OutputMode.Update())

      .format("console")
      .start
    query.awaitTermination()

    spark.stop()
  }
}
