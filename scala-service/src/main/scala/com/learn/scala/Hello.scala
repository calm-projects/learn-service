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

