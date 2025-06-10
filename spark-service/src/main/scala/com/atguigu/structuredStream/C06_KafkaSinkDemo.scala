package com.atguigu.structuredStream

/**
 * 描述信息
 *
 * @create: 2023-03-25 23:55
 */
object C06_KafkaSinkDemo {

//  // Subscribe to 1 topic
//  val df = spark
//    .readStream
//    .format("kafka")
//    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
//    .option("subscribe", "topic1")
//    .load()
//  df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
//    .as[(String, String)]
//
//  // Subscribe to 1 topic, with headers
//  val df = spark
//    .readStream
//    .format("kafka")
//    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
//    .option("subscribe", "topic1")
//    .option("includeHeaders", "true")
//    .load()
//  df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)", "headers")
//    .as[(String, String, Array[(String, Array[Byte])])]
//
//  // Subscribe to multiple topics
//  val df = spark
//    .readStream
//    .format("kafka")
//    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
//    .option("subscribe", "topic1,topic2")
//    .load()
//  df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
//    .as[(String, String)]
//
//  // Subscribe to a pattern
//  val df = spark
//    .readStream
//    .format("kafka")
//    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
//    .option("subscribePattern", "topic.*")
//    .load()
//  df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
//    .as[(String, String)]
//
//  // Subscribe to 1 topic defaults to the earliest and latest offsets
//  val df = spark
//    .read
//    .format("kafka")
//    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
//    .option("subscribe", "topic1")
//    .load()
//  df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
//    .as[(String, String)]
//
//  // Subscribe to multiple topics, specifying explicit Kafka offsets
//  val df = spark
//    .read
//    .format("kafka")
//    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
//    .option("subscribe", "topic1,topic2")
//    .option("startingOffsets", """{"topic1":{"0":23,"1":-2},"topic2":{"0":-2}}""")
//    .option("endingOffsets", """{"topic1":{"0":50,"1":-1},"topic2":{"0":-1}}""")
//    .load()
//  df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
//    .as[(String, String)]
//
//  // Subscribe to a pattern, at the earliest and latest offsets
//  val df = spark
//    .read
//    .format("kafka")
//    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
//    .option("subscribePattern", "topic.*")
//    .option("startingOffsets", "earliest")
//    .option("endingOffsets", "latest")
//    .load()
//  df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
//    .as[(String, String)]




  // Write key-value data from a DataFrame to a specific Kafka topic specified in an option
//  val ds = df
//    .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
//    .writeStream
//    .format("kafka")
//    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
//    .option("topic", "topic1")
//    .start()
//
//  // Write key-value data from a DataFrame to Kafka using a topic specified in the data
//  val ds = df
//    .selectExpr("topic", "CAST(key AS STRING)", "CAST(value AS STRING)")
//    .writeStream
//    .format("kafka")
//    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
//    .start()
//
//  df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
//    .write
//    .format("kafka")
//    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
//    .option("topic", "topic1")
//    .save()
//
//  // Write key-value data from a DataFrame to Kafka using a topic specified in the data
//  df.selectExpr("topic", "CAST(key AS STRING)", "CAST(value AS STRING)")
//    .write
//    .format("kafka")
//    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
//    .save()

}
