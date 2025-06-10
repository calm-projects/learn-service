package com.wisers.sql

import org.apache.spark.sql.{DataFrame, Row, SaveMode}
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StructType}

import scala.collection.immutable

/**
 * parquet
 * json
 * jdbc
 * hive
 */
class TestReadAndWrite extends UnitSpecSparkSql{
  import spark.implicits._
  val path = "spark-demo/src/main/scala/com/wisers/output/sql/";

  test("test parquet") {
    val list: immutable.Seq[(String, Int, String)] = List(("angelbaby", 18, "中国"), ("tom", 18, "美国"),
      ("jack", 18, "美国"), ("rose", 18, "法国"))
    val frame01: DataFrame = list.toDF("name", "age", "country")

    // frame.write.mode(SaveMode.Overwrite).partitionBy("name").parquet(path + "partitionBy")
    frame01.write.mode(SaveMode.Overwrite).partitionBy("name").parquet(path + "test.parquet")

    val frame02 = spark.read.parquet(path + "test.parquet")
    val partitions: Int = frame02.rdd.getNumPartitions
    assertResult(4)(partitions)
    frame02.show(false)
  }

  test("test json") {
    val list: immutable.Seq[(String, Int, String)] = List(("angelbaby", 18, "中国"), ("tom", 18, "美国"),
      ("jack", 18, "美国"), ("rose", 18, "法国"))
    val frame01: DataFrame = list.toDF("name", "age", "country")

    // frame.write.mode(SaveMode.Overwrite).partitionBy("name").parquet(path + "partitionBy")
    frame01.write.mode(SaveMode.Overwrite).partitionBy("name").json(path + "test.json")

    val frame02 = spark.read.json(path + "test.json")
    val partitions: Int = frame02.rdd.getNumPartitions
    assertResult(4)(partitions)
    frame02.show(false)
  }

  test("test jdbc") {
    val list: immutable.Seq[(String, Int, String)] = List(("angelbaby", 18, "中国"), ("tom", 18, "美国"),
      ("jack", 18, "美国"), ("rose", 18, "法国"))
    val frame01: DataFrame = list.toDF("name", "age", "country")

    // frame.write.mode(SaveMode.Overwrite).partitionBy("name").parquet(path + "partitionBy")
    frame01.write.mode(SaveMode.Overwrite).partitionBy("name").json(path + "test.json")

    val frame02 = spark.read.json(path + "test.json")
    val partitions: Int = frame02.rdd.getNumPartitions
    assertResult(4)(partitions)
    frame02.show(false)
  }


}
