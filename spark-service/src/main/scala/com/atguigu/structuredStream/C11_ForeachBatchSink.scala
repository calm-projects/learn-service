package com.atguigu.structuredStream

import com.atguigu.core.rdd.operator.action.Spark07_RDD_Operator_Action.User

import java.util.Properties
import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}

/**
 * 描述信息
 *
 * @create: 2023-03-26 02:25
 */
object C11_ForeachBatchSink {
  case class word_count(name: String, cnt: Long)
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[2]")
      .appName("ForeachBatchSink")
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
      .option("kafka.group.id", "test_011")
      .load

    val lines = df.selectExpr("CAST(value AS STRING)")
      .as[String]

    val wordCount = lines.as[String]
      .flatMap(_.split("\\W+"))
      .groupBy("value")
      .count()

    val frame = wordCount.selectExpr("count as cnt", "value as name")


    val props = new Properties()
    props.setProperty("user", "root")
    props.setProperty("password", "123456")

    val query: StreamingQuery = frame.writeStream
      .outputMode(OutputMode.Update())
      .option("checkpointLocation", "datas/atGuiGu/StreamStruct/ck1") // 不设置checkpoint还会重新消费
      .foreachBatch{(df: DataFrame, batchId: Long) =>
        df.printSchema()
        df.show(false)
        df.cache()
        // df.write.json(s"datas/atGuiGu/StreamStruct/ck7/$batchId")
        df.write.mode(SaveMode.Append).jdbc("jdbc:mysql://localhost:3306/test", "test", props)
    }.start()
    query.awaitTermination()

  }
}
