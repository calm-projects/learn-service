package com.tmp

import com.wisers.UnitSpec
import com.wisers.sql.UnitSpecSparkSql
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.{col, column, lit}
import org.apache.spark.sql.types.{StringType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row}

import scala.collection.immutable

/**
 * 描述信息
 */
class TmpTest extends UnitSpecSparkSql{
  test("test top") {
    val dataSourceRDD: RDD[String] = spark.sparkContext.makeRDD(List("name hello world", "tom jack hello tom tom"))
    val flatMapRdd: RDD[String] = dataSourceRDD.flatMap(_.split(" "))
    val mapRdd: RDD[(String, Int)] = flatMapRdd.map((_, 1))
    val reduceRdd: RDD[(String, Int)] = mapRdd.reduceByKey(_ + _)

    println(reduceRdd.dependencies)
    println(reduceRdd.getNumPartitions)
    reduceRdd.collect().foreach(println(_))

    val sortRdd: RDD[(String, Int)] = reduceRdd.sortBy(_._2, false)
    println(sortRdd.dependencies)
    println(sortRdd.toDebugString)
    println(sortRdd.getNumPartitions)
    println("_"*50)
    sortRdd.collect().foreach(println(_))
    println("_"*50)
    val tuples: Array[(String, Int)] = sortRdd.take(2)
    tuples.foreach(println(_))
  }

  test("测试 withColumn 和 lit") {
    val dataRows = Seq(Row(Row("James;", "", "Smith"), "36636", "M", "3000"),
      Row(Row("Michael", "Rose", ""), "40288", "M", "4000"),
      Row(Row("Robert", "", "Williams"), "42114", "M", "4000"),
      Row(Row("Maria", "Anne", "Jones"), "39192", "F", "4000"),
      Row(Row("Jen", "Mary", "Brown"), "", "F", "-1")
    )

    val schema = new StructType()
      .add("name", new StructType()
        .add("firstname", StringType)
        .add("middlename", StringType)
        .add("lastname", StringType))
      .add("dob", StringType)
      .add("gender", StringType)
      .add("salary", StringType)

    val df2 = spark.createDataFrame(spark.sparkContext.parallelize(dataRows),schema)
    //Change the column data type
    df2.withColumn("salary", col("salary").cast("Integer")).printSchema()
    println("Change the column data type"*5)

    //Derive a new column from existing
    val df4 = df2.withColumn("CopiedColumn", column("salary") * -1)
    //  val df4 = df2.withColumn("CopiedColumn", df2("salary") * -1)
      df4.show(false)

    println("Derive a new column from existing"*5)

    //Transforming existing column
    val df5 = df2.withColumn("salary", df2("salary") * 100).show(false)

    println("Transforming existing column"*5)
    //You can also chain withColumn to change multiple columns

    //Renaming a column.
    val df3 = df2.withColumnRenamed("gender", "sex")
    df3.printSchema()
    println("Renaming a column."*5)

    //Droping a column
    val df6 = df4.drop("CopiedColumn")
    println(df6.columns.contains("CopiedColumn"))
    println("Droping a column."*5)
    //Adding a literal value
    val frame = df2.withColumn("Country", lit("USA"))
    frame.show(false)


    println("add a column."*5)

    //Retrieving
    df2.show(false)
    df2.select("name").show(false)
    df2.select("name.firstname").show(false)
    df2.select("name.*").show(false)

  }

  test("利用map将一个字段转为多个字段") {
    import spark.implicits._

    val columns = Seq("name", "address")
    val data = Seq(("Robert, Smith", "1 Main st, Newark, NJ, 92537"),
      ("Maria, Garcia", "3456 Walnut st, Newark, NJ, 94732"))
    val dfFromData: DataFrame = spark.createDataFrame(data).toDF(columns: _*)
    dfFromData.printSchema()

    val newDF: Dataset[(String, String, String, String, String, String)] = dfFromData.map(f => {
      val nameSplit = f.getAs[String](0).split(",")
      val addSplit = f.getAs[String](1).split(",")
      (nameSplit(0), nameSplit(1), addSplit(0), addSplit(1), addSplit(2), addSplit(3))
    })
    val finalDF = newDF.toDF("First Name", "Last Name",
      "Address Line1", "City", "State", "zipCode")
    finalDF.printSchema()
    finalDF.show(false)

  }
}

object TmpTest {

  case class Person2(id: Int, age: Int, xx: Int)
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
    conf.setAppName("")
    conf.setMaster("local[1]")

    val sc = new SparkContext(conf)


    val rdd1: RDD[String] = sc.parallelize(Seq("a a a a a b", "c c c a b c a c", "d a f g a c d f"))
    val rdd2: RDD[Int] = sc.parallelize(Seq(1, 2, 3, 4, 5, 6, 7, 8))
    val rdd3: RDD[Person2] = sc.parallelize(Seq(Person2(1, 18, 9888), Person2(2, 28, 6800), Person2(3, 24, 12000)))


    implicit val ord: Ordering[Person2] = new Ordering[Person2] {
      override def compare(x: Person2, y: Person2): Int = x.age.compare(y.age)
    }

    // take
    val strings1: Array[String] = rdd1.takeOrdered(5)
    val persons: Array[Person2] = rdd3.takeOrdered(2)

    println(strings1.toBuffer)
    println(persons.toBuffer)

    sc.stop()
  }
}