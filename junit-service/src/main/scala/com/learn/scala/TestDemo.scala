package com.learn.scala

class TestDemo extends UnitSpec {
  override protected def beforeAll(): Unit = {}

  override protected def afterAll(): Unit = {}

  test("base test") {
    assertThrows[ArithmeticException](m1())
    assertResult("预期的值")("实际的值")
    assert("tom" == "tom")
  }

  def m1(): Unit = {
    val num = 10 / 0
  }

  test("exception") {
    try {
      val num = 10 / 0
    } catch {
      case ex: ArithmeticException => throw new Exception("抛出异常")
      case ex: Exception => println("其他异常")
    } finally {
      println("关闭资源")
    }
  }
}
