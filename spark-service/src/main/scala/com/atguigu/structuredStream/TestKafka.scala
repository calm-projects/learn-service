package com.atguigu.structuredStream

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode
import org.scalatest.funsuite.AnyFunSuite

class TestKafka extends AnyFunSuite {
  /*
    [hadoop@hadoop03 bin]$ kafka-topics.sh --bootstrap-server hadoop03:9092 --topic test --describe
    Topic: test	TopicId: RhSVrKg_RpeIVu9D_btyhw	PartitionCount: 3	ReplicationFactor: 2	Configs:
    Topic: test	Partition: 0	Leader: 0	Replicas: 0,2	Isr: 0,2
    Topic: test	Partition: 1	Leader: 2	Replicas: 2,0	Isr: 2,0
    Topic: test	Partition: 2	Leader: 0	Replicas: 0,2	Isr: 0,2

    [hadoop@hadoop03 bin]$ kafka-console-consumer.sh --bootstrap-server hadoop03:9092 --topic test --from-beginning

    nc -l 9999

   */
  test("write kafka") {
    val spark = SparkSession.builder().master("local[3]").appName("天官赐福").getOrCreate()
    val lines = spark.readStream.format("socket").option("host", "hadoop03").option("port", 9999).load()
    lines.createOrReplaceTempView("words")
    val jsonDf = spark.sql(
      """
        |select to_json(named_struct('name', value)) as value from words
        |""".stripMargin)

    // jsonDf.selectExpr("CAST(value AS STRING)").writeStream.format("console").outputMode(OutputMode.Append()).start().awaitTermination()
    jsonDf
      // 这句话可以去掉，本身就是字符串了
      // .selectExpr("CAST(value AS STRING)")
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "hadoop03:9092")
      .option("topic", "test")
      .option("kafka.acks", "all")
      // 本地会多出一个checkpoint路径，集群环境就是hdfs
      .option("checkpointLocation", "checkpoint")
      .start()
      .awaitTermination()
  }

  test("read kafka") {
    val spark = SparkSession.builder().master("local[3]").appName("天官赐福").getOrCreate()
    import spark.implicits._
    val kafkaDf = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "hadoop03:9092")
      /*
        订阅topic 写法
       .option("subscribe", "topic1,topic2")
        .option("subscribePattern", "topic.*")
       */
      .option("subscribe", "test")
      /*
        可以设置读取偏移量， 默认startingOffsets为latest endingOffsets 为latest
        .option("startingOffsets", """{"topic1":{"0":23,"1":-2},"topic2":{"0":-2}}""")
        .option("endingOffsets", """{"topic1":{"0":50,"1":-1},"topic2":{"0":-1}}""")
        .option("startingOffsets", "earliest")
        .option("endingOffsets", "latest")
       */
      .option("startingOffsets", "earliest")
      .option("includeHeaders", "true")
      .option("failOnDataLoss", value = false)
      .option("maxOffsetsPerTrigger", 44000)
      .option("kafka.max.partition.fetch.bytes", 3145728)
      .load()
    val dataDf = kafkaDf.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)", "headers")
      .as[(String, String, Array[(String, Array[Byte])])]
    dataDf
      .writeStream
      .format("console")
      .outputMode(OutputMode.Append())
      // 必须设置否则会重新消费，设置完故障恢复的时候会根据checkpoint恢复
      .option("checkpointLocation", "checkpoint-consumer")
      .start()
      .awaitTermination()
  }
}
