package com.tmp

import com.wisers.UnitSpec
import com.wisers.sql.UnitSpecSparkSql
import io.delta.tables.DeltaTable
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.types.{DataTypes, StructField}
import org.json4s.DoubleJsonFormats.GenericFormat
import org.json4s.Formats
import org.json4s.JsonAST.{JArray, JObject, JString}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{compact, render}
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.immutable
import scala.util.parsing.json.{JSONArray, JSONObject}

class DeltaTest extends UnitSpecSparkSql{
  import spark.implicits._
  val path = "datas/output/delta"

  /**
   * 默认数据源 spark.sql.sources.default parquet
   * bucketBy 只能和saveAsTable一起使用（delta可能不支持），使用sortBy和下面的repartition是一样的
   * 在partitionBy 之前用repartition(根据分区字段) 是将同分区的数据集中到一个task或者一个分区，这样不会形成很多小文件
   *
   * @return
   */

  test("write delta") {
    val list = List(("tom1", 18, 20220314), ("tom2", 19, 20220315),("tom2", 19, 20220316), ("jack", 20, 20220316))
    val frame = list.toDF("name", "age", "date")
    println(frame.rdd.getNumPartitions)
    frame.write.format("delta")
      .partitionBy("date")
      .save(path)

//    frame.write.format("delta")
////      .sortBy("date") // 会进行shuffle
//      .bucketBy(3, "date")
//      .partitionBy("name")
//      .saveAsTable("testDelta")

    frame.write.partitionBy("date").parquet(path+2)
    frame.write
      .format("parquet")
      .bucketBy(12, "date")
      .partitionBy("name")
      .option("path", "datas/testDelta/kol")
      .saveAsTable("testDelta")

  }

  test("aaa") {
    val table = DeltaTable.forPath(path).vacuum(0)
    table
  }

  test("test parquet") {
    val sql =
    """
      | select  CAST(date_trunc('WEEK', current_timestamp()) - interval 1 weeks AS TIMESTAMP), date_trunc('WEEK', now()) as c,
      |  from_json("[1,2,3]", array<Int>), to_json("[1,2,3]", array<Int>)
      |
      |""".stripMargin
    val frame = spark.sql(sql)
    frame.printSchema()
    frame.show(false)
  }

  test("test 1parquet") {
    val sql =
      """
        | select  concat_ws(';',array_distinct(filter(array(""," "), x -> instr(" tom  jack",x) !=0 and trim(x) <> '' and isnotnull(x)))) as c,
        | case when profession_array is null then null
        |when size(profession_array) == 1 then element_at(profession_array, 1).name
        |else concat_ws(';',filter(from_json(get_json_object(to_json(profession_array), '$[*].name'), 'array<string>'), x -> trim(x) <> '' and isnotnull(x)))
        |end as hash_career
        |
        |""".stripMargin
    val frame = spark.sql(sql)
    frame.printSchema()
    frame.show(false)
  }


  test("read delta")  {
    val frame = spark.read.format("delta").load(path)
    frame.printSchema()
    frame.show(false)
  }

  test("test regexp_extract") {
    val strings = "#tom#jack[aaa]##".split("#").filter(_.nonEmpty)
        .map(_.replaceAll("\\[aaa\\]", ""))



    val arr: Seq[String] = for(a <- List("a", "b")) yield a

    val str: String = compact(render(strings.toSeq))
    println(str)
    arr
    JArray(List("a","b"))
    println(Array("a","b").toBuffer.mkString)
    val sql =
      """
        |select "jack,tom,rose",
        |from_json(ifnull('["tom","rose"]', '[]'), 'array<string>') urlList
        |""".stripMargin
    spark.sql(sql).show(false)
    spark.sql("SELECT regexp_extract('#你好#tom##jack#', '#([^#.*?])+#', 1)").show(false)
  }

  test("test df")  {
    val list = List(("tom1", 18, 20220314), ("tom2", 19, 20220315), ("tom2", 19, 20220315))
//    list.map()
    val frame = list.toDF("name", "age", "date")
    frame.createTempView("tmp")
    val sql =
      """
        |select distinct(*) from tmp
        |""".stripMargin
    spark.sql(sql).show(false)
    // frame.dropDuplicates("name").show(false)
  }

  test("rename delta") {

    val tableName = "default.events"

    val sql =
      s"""
         |CREATE TABLE ${tableName}
         |USING DELTA
         |LOCATION '${path}'
         |""".stripMargin
    spark.sql(sql)

    spark.read.table(tableName)
      .withColumnRenamed("name", "name_new")
      .write
      .format("delta")
      .mode("overwrite")
      .option("overwriteSchema", "true")
      .saveAsTable(tableName)

    val frame1 = spark.read.format("delta").load(path)
    frame1.printSchema()
    frame1.show(false)

  }

  test("order test") {
     val list0 = List(("tom1", 19, null, 20220315), ("tom1", 18, "123", 20220314),  ("tom1", 20, null, 20220316))
     val df = list0.toDF("name", "age", "dst", "date")
    df.show(false)
    df.orderBy(desc("dst")).show(false)
    df.orderBy(desc("dst")).dropDuplicates("name").show(false)
  }

  test("order test1") {
    val list0 = List(("tom1", 19, null, 20220315), ("tom1", 18, "123", 20220314),  ("tom1", 20, null, 20220316))
    val df = list0.toDF("name", "age", "dst", "date")
    df.createTempView("tmp")
    val sql =
      """
        |SELECT named_struct("a", 1, "b", 2, "c", null).c c0,
        |       to_json(named_struct("a", 1, "b", 2, "c", null).c) c1,
        |       to_json(null) c2
        |from tmp
        |""".stripMargin
    val frame = spark.sql(sql)
    frame.createTempView("tmp2")
    val sql2 =
      """
        |select * from tmp2 where c1 is null and c0 is null and c2 is null
        |""".stripMargin
    val frame1 = spark.sql(sql2)
    frame1.printSchema()
    frame1.show(false)
//    df.orderBy(desc("dst")).show(false)
//    df.orderBy(desc("dst")).dropDuplicates("name").show(false)
  }


  test("scheme test") {
    val list0 = List(("{\"name-desc\":\"name-tom\"}", null))
    val df1 = list0.toDF("name", "url")
    df1.createTempView("tmp")
    val sql =
      """
        |select json_tuple(name, 'name-desc') as (desc),
        |       url,
        |       array(null) as a,
        |       from_json(ifnull(null, '[]'), 'array<string>') urlList,
        |       ifnull(
        |         concat(concat('https://www.kuaishou.com/short-video/',substring_index(parse_url(split(url,'_low.webp')[0],'QUERY','clientCacheKey'),'_',1),'?photoid=','31231231231'),";W;1")
        |         ,
        |         '222'
        |        )as urlList1,
        |       array('')
        |       from tmp
        |""".stripMargin
    spark.sql(sql).show(false)
    val df: DataFrame = spark.sql(sql)
    val schema = df.schema
    /*val schema = df.schema
    val rows = df.map{
      row => {
        val list = row.getAs("urlList").asInstanceOf[Seq[String]].toList
        println(list)
        for(l <- list){
        }
        val list2 = row.getAs("url").asInstanceOf[Seq[String]].toList
        for(l <- list2){
          println(l)
        }
        row
      }
    }(RowEncoder(schema))*/

    val res = df.mapPartitions(iter => {
      val rows = iter.map(row => {
        val seqArr = new Array[Any](schema.length)
        // copy the row's content to the seq
        for (index <- 0 until row.length) {
          seqArr.update(index, row.get(index))
        }
        val list = row.getAs("urlList").asInstanceOf[Seq[String]].toList
        println("nnnnnnnnnnnnnnnn"+ list)
        for(l <- list if l.endsWith(",")){
          println("nnnnnnnnnnnnnnnn111111111111111111111"+ list)
        }
        seqArr.update(schema.fieldIndex("url"), "abc123")
        val newRow: Row = new GenericRowWithSchema(seqArr, schema)
        newRow
      })
      rows
    })(RowEncoder(schema))
    res.show(false)
    res.printSchema()

//    val list1: Seq[Any] = spark.sql(sql).select("a").collect().map(_ (0)).toList
//    for(l <- list1){
//      println(l)
//    }
//    println(list1)
  }


  test("arary test"){
    val list0 = List(("{\"user\":{\"user_sex\":\"F\",\"eid\":\"3xpxe43us2m38ts\",\"user_id\":152100062,\"user_name\":\"虹虹80后男鞋\",\"following\":false,\"headurl\":\"http://p4.a.yximgs.com/uhead/AB/2018/06/28/22/BMjAxODA2MjgyMjQwMzlfMTUyMTAwMDYyXzJfaGQ2NDFfMjgx_s.jpg\",\"profilePagePrefetchInfo\":{\"profilePageType\":2},\"headurls\":[{\"cdn\":\"p4.a.yximgs.com\",\"url\":\"http://p4.a.yximgs.com/uhead/AB/2018/06/28/22/BMjAxODA2MjgyMjQwMzlfMTUyMTAwMDYyXzJfaGQ2NDFfMjgx_s.jpg\"},{\"cdn\":\"p2.a.yximgs.com\",\"url\":\"http://p2.a.yximgs.com/uhead/AB/2018/06/28/22/BMjAxODA2MjgyMjQwMzlfMTUyMTAwMDYyXzJfaGQ2NDFfMjgx_s.jpg\"}],\"visitorBeFollowed\":false,\"kwaiId\":\"yayaxiaobao\"}}")
      , (null))
    val df1 = list0.toDF("music")
    df1.createTempView("tmp")
    val sql =
      """
        | select json_tuple(get_json_object(music, '$.user'), 'user_id', 'eid') as (user_id, eid) from tmp
        |""".stripMargin
    val frame = spark.sql(sql)
    frame.printSchema()
    frame.show(false)
  }



  test("merge delta") {
    val Int = 1645165320
   /* val list0 = List(("tom1", 18, null, 20220314), ("tom2", 19, null, 20220315), ("tom2", 20, null, 20220316))
    val df = list0.toDF("name", "age", "dst", "date")
    df.createTempView("tmp")
    val sql =
      """
        |select name, age, current_timestamp() as dst , date from tmp
        |""".stripMargin
    spark.sql(sql).write.format("delta").partitionBy("date").save(path+"3")

    DeltaTable.forPath(path+"3").toDF.show(false)*/


    val list = List(("tom1", 18, null, 20220314), ("tom2", 19, null, 20220315), ("tom3", 20, null, 20220316))
    val updatesDF = list.toDF("name", "age", "dst", "date")


    DeltaTable.forPath(spark, path+"3")
      .as("events")
      .merge(
        updatesDF.as("updates"),
        "events.name = updates.name")
      .whenMatched
      .updateExpr(
        Map("dst" -> "date_format(current_timestamp(), 'yyyy-MM-dd HH:mm:ss')"))
      .whenNotMatched
      .insertExpr(
        Map(
          "name" -> "updates.name",
          "age" -> "updates.age",
          "date" -> "updates.date"))
      .execute()

    DeltaTable.forPath(path+"3").toDF.show(false)
  }
}
