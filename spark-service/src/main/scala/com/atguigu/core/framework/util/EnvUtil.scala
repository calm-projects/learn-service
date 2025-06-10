package com.atguigu.core.framework.util

import org.apache.spark.SparkContext

import scala.collection.mutable.ArrayBuffer

object EnvUtil {

    private val scLocal = new ThreadLocal[SparkContext]()
    //这里也可以使用其他结构，但是ThreadLocal还有其他的作用暂时先不考虑了等看线程安全的时候在看
    // private val scLocal = ArrayBuffer[SparkContext]()

    def put( sc : SparkContext ): Unit = {
        scLocal.set(sc)
    }

    def take(): SparkContext = {
        scLocal.get()
    }

    def clear(): Unit = {
        scLocal.remove()
    }
}
