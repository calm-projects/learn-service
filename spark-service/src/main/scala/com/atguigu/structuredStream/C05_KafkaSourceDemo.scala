package com.atguigu.structuredStream

import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{DataFrame, SparkSession}

object C05_KafkaSourceDemo {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("KafkaSourceDemo")
      .getOrCreate()
    
    
    /**
     * subscribe 指定主题 "topic1,topic2"
     * subscribePattern  topic.*
     * startingOffsets """{"topic1":{"0":23,"1":-2},"topic2":{"0":-2}}"""
     *  -2 == earliest -1  == latest
     *   "earliest", "latest" (streaming only), or json string """ {"topicA":{"0":23,"1":-1},"topicB":{"0":-2}} """
     * endingOffsets  """{"topic1":{"0":50,"1":-1},"topic2":{"0":-1}}"""
     *  latest or json string {"topicA":{"0":23,"1":-1},"topicB":{"0":-1}}
     *  batch query
     * kafkaConsumer.pollTimeoutMs  120000
     * fetchOffset.numRetries	 3
     * fetchOffset.retryIntervalMs	10
     * maxOffsetsPerTrigger none  streaming
     * minOffsetsPerTrigger none  streaming
     * maxTriggerDelay 15m streaming
     * kafka.group.id none streaming and batch
     * includeHeaders false streaming and batch
     */
    
    // 得到的 df 的 schema 是固定的: key,value,topic,partition,offset,timestamp,timestampType
    val df: DataFrame = spark.readStream
      .format("kafka") // 设置 kafka 数据源
      .option("kafka.bootstrap.servers", "hadoop01:9092,hadoop02:9092,hadoop03:9092")
      .option("subscribe", "test") // 也可以订阅多个主题:   "topic1,topic2"
      .load


    df.writeStream
      .outputMode("update")
      .format("console")
      .trigger(Trigger.Continuous(1000))
      .start
      .awaitTermination()

  }
}