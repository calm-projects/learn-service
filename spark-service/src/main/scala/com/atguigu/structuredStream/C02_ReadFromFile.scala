package com.atguigu.structuredStream

import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery, Trigger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}

/**
 * 描述信息
 *
 * @create: 2023-03-25 23:05
 */
object C02_ReadFromFile {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("ReadFromFile")
      .getOrCreate()

    // 定义 Schema, 用于指定列名以及列中的数据类型
    val userSchema: StructType = new StructType().add("name", StringType).add("sex", StringType).add("age", IntegerType)

    /**
     * maxFilesPerTrigger  每次最多触发文件数
     * latestFirst 当文件积压时是否首先处理最新文件 false
     * fileNameOnly 仅仅匹配文件名代替全路径，下面认为时相同的 默认false
     * "file:///dataset.txt"
     * "s3://a/dataset.txt"
     * maxFileAge 在该目录中可以找到的文件在被忽略之前的最大年龄。对于第一批文件，所有文件都被认为是有效的 如果 maxFilesPerTrigger被设置
     *  并且latestFirst位ture 则忽略
     *
     *
     * @param args
     * @return void
     */

    val user: DataFrame = spark.readStream
      .schema(userSchema)
      .csv("datas/atGuiGu/StreamStruct/csv")

    val query: StreamingQuery = user.writeStream
      .outputMode(OutputMode.Append())
      .trigger(Trigger.ProcessingTime(0)) // 触发器 数字表示毫秒值. 0 表示立即处理
      .format("console")
      .start()
    query.awaitTermination()
  }
}
