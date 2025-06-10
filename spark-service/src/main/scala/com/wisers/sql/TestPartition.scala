package com.wisers.sql

import org.apache.spark.sql.{DataFrame, SaveMode}

import scala.collection.immutable

/**
 * 描述信息
 */
class TestPartition extends UnitSpecSparkSql {
  import spark.implicits._
  val path = "spark-demo/src/main/scala/com/wisers/output/sql/";

  test("test partitionBy"){
    val list: immutable.Seq[(String, Int, String)] = List(("angelbaby", 18, "中国"), ("tom", 18, "美国"),
        ("jack", 18, "美国"), ("rose", 18, "法国"))
    val frame: DataFrame = list.toDF("name", "age", "country")

    frame.write.mode(SaveMode.Overwrite).partitionBy("name")
      .json(path + "partitionBy")
  }

  /**
   * 分桶直接save的目前2.4.3还未实现 报错： 'save' does not support bucketBy and sortBy right now;
   * saveAsTable 应该可以未进行测试
   */
  test("test bucketBy"){
    val list: immutable.Seq[(String, Int, String)] = List(("angelbaby", 18, "中国"), ("tom", 18, "美国"),
      ("jack", 18, "美国"), ("rose", 18, "法国"))
    val frame: DataFrame = list.toDF("name", "age", "country")

//    frame.write.mode(SaveMode.Overwrite).bucketBy(3, "name")
//      .json(path + "bucketBy")

    frame.write.mode(SaveMode.Overwrite).bucketBy(3, "name")
      .saveAsTable("bucketBy")
  }
}
