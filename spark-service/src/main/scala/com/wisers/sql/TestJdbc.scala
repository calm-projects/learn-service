package com.wisers.sql

import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

/**
 * 描述信息
 */
class TestJdbc  extends AnyFunSuite with BeforeAndAfterAll{
  val spark = SparkSession
    .builder()
    .appName("天官赐福")
    .master("local[*]")
    .getOrCreate()

  test("read mysql to kafka") {
    val url = "jdbc:mysql://localhost:3306/test?useSSL=false&useUnicode=true&characterEncoding=utf-8"
    val jdbcDF = spark.read
      .format("jdbc")
      .option("url", url)
      .option("dbtable", "test.person")
      .option("user", "root")
      .option("password", "abc123!@#")
      .load()
  }
  override protected def afterAll(): Unit = {
    println("===================================================================")
    spark.close()
  }
}
