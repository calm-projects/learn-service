package com.learn.scala


object Hello {
  def main(args: Array[String]): Unit = {
    println("hello scala")
    System.out.println("hello scala")

    def ops(x: Int, y: Int, op: (Int, Int) => Int) = {
      op(x, y)
    }

    println(ops(1, 2, _ + _))
  }
}

object TestWordCount {
  def main(args: Array[String]): Unit = {
    val words = List(("Hello Scala Spark World ", 4), ("Hello Scala Spark", 3), ("Hello Scala", 2), ("Hello", 1))
    val wordCount = words.flatMap(ele => {
        ele._1.split(" ").map((_, ele._2))
      })
      .groupBy(_._1)
      .map(item => (item._1, item._2.map(_._2).sum))
      .toList
      .sortBy(_._2)
      .reverse
    println(wordCount)
  }
}


class TestCompare1[T: Ordering](obj1: T, obj2: T) {
  def greater: T = {
    val comparator = implicitly[Ordering[T]]
    if (comparator.compare(obj1, obj2) > 0) obj1 else obj2
  }
}

class TestCompare2[T](obj1: T, obj2: T)(implicit comp: Ordering[T]) {
  def greater(): T = if (comp.compare(obj1, obj2) > 0) obj1 else obj2
}


import scala.math.Ordering.Implicits._

class TestCompare3[T: Ordering](obj1: T, obj2: T) {
  def greater: T = obj1.max(obj2) // 也可以写 if (obj1 > obj2)
}

object Test {
  def main(args: Array[String]): Unit = {
    println(new TestCompare1(1, 2).greater)
    println(new TestCompare2(1, 2).greater())
    println(new TestCompare3(1, 2).greater)
  }
}
