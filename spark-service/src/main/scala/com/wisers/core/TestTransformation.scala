package com.wisers.core

import com.wisers.UnitSpec
import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD

/**
 * spark的算子和scala的function其实很像
 */
class TestTransformation extends UnitSpec{

  /**
   * 创建rdd
   * makeRDD 底层调用的parallelize，不传递并行度默认为当前计算机的core
   * parallelize
   * textFile
   * 其他rdd转换
   */
  test("test makeRDD & parallelize") {
    val makeRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4), 3)
    val parallelizeRdd: RDD[Int] = spark.parallelize(List(1, 2, 3, 4))

    println(makeRdd.getNumPartitions)  // 3
    println(parallelizeRdd.getNumPartitions)  // .setMaster("local[*]")
  }

  /**
   * map 对数据逐条转换
   */
  test("test map") {
    val makeRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4), 3)
    // 2 4 6 8
    makeRdd.map(_*2).collect().foreach(println)
  }

  /**
   * mapPartitions 将待处理的数据以分区为单位发送到计算节点进行处理，这里的处理是指可以进行任意的处理，哪怕是过滤数据。
   * map vx mapPartitions
   * 数据处理角度
   * Map算子是分区内一个数据一个数据的执行，类似于串行操作。而mapPartitions算子是以分区为单位进行批处理操作。
   * 功能的角度
   * Map算子主要目的将数据源中的数据进行转换和改变。但是不会减少或增多数据。MapPartitions算子需要传递一个迭代器，
   *  返回一个迭代器，没有要求的元素的个数保持不变，所以可以增加或减少数据
   * 性能的角度
   * Map算子因为类似于串行操作，所以性能比较低，而是mapPartitions算子类似于批处理，所以性能较高。但是mapPartitions算子会长时间占用内存，
   *  那么这样会导致内存可能不够用，出现内存溢出的错误。所以在内存有限的情况下，不推荐使用。使用map操作。
   */
  test("test mapPartitions") {
    val makeRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4), 3)
    // 2 4
    val value: RDD[Int] = makeRdd.mapPartitions(_.filter(_ % 2 == 0))
    value.collect().foreach(println)
  }


  /**
   * mapPartitionsWithIndex 将待处理的数据以分区为单位发送到计算节点进行处理，这里的处理是指可以进行任意的处理，哪怕是过滤数据，
   *  在处理时同时可以获取当前分区索引。
   */
  test("test mapPartitionsWithIndex") {
    val makeRdd: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4), 3)
    val value: RDD[(Int, Int)] = makeRdd.mapPartitionsWithIndex((index, datas) => {
      datas.map((index, _))
    })
    // (0,1) (1,2) (2,3) (2,4)
    value.collect().foreach(println)
  }

  /**
   * flatMap
   */
  test("test flatMap") {
    val dataRDD = spark.makeRDD(List(List(1,2), List(3,4)),1)
    val value: RDD[Int] = dataRDD.flatMap(list => list)
    // 1, 2, 3, 4
    value.collect().foreach(println)
  }

  /**
   * groupBy 将数据根据指定的规则进行分组, 分区默认不变，但是数据会被打乱重新组合，我们将这样的操作称之为shuffle。
   *  极限情况下，数据可能被分在同一个分区中， 一个组的数据在一个分区中，但是并不是说一个分区中只有一个组
   */
  test("test groupBy") {
    val dataRDD = spark.makeRDD(List(1, 2, 3, 4), 1)
    val value: RDD[(Int, Iterable[Int])] = dataRDD.groupBy(_ % 2)
    // (0,CompactBuffer(2, 4)) (1,CompactBuffer(1, 3))
    value.collect().foreach(println)

    val dataRDD02 = spark.makeRDD(List("Hello", "hive", "hbase", "Hadoop"), 1)
    val value02: RDD[(Char, Iterable[String])] = dataRDD02.groupBy(_.charAt(0))
    // (h,CompactBuffer(hive, hbase)) (H,CompactBuffer(Hello, Hadoop))
    value02.collect().foreach(println)
  }

  /**
   * filter
   *  将数据根据指定的规则进行筛选过滤，符合规则的数据保留，不符合规则的数据丢弃。
   *  当数据进行筛选过滤后，分区不变，但是分区内的数据可能不均衡，生产环境下，可能会出现数据倾斜。
   */
  test("test filter") {
    val dataRDD = spark.makeRDD(List(1, 2, 3, 4), 1)
    val value: RDD[Int] = dataRDD.filter(_ % 2 == 0)
    // 2 4
    value.collect().foreach(println)
  }

  /**
   * sample
   *  返回采样子集
   *  第一个参数： withReplacement 元素可以被多次采样吗 （伯努利算法）
   *    抽取数据不放回（伯努利算法）
   *    抽取数据放回（泊松算法）
   *  第二个参数：
   *    抽取的几率，范围在[0,1]之间,0：全不取；1：全取；
   *    重复数据的几率，范围大于等于0.表示每一个元素被期望抽取到的次数
   *  第三个参数：随机数种子
   */
  test("test sample") {
    val dataRDD = spark.makeRDD(List(1, 2, 3, 4), 1)
    val value01: RDD[Int] = dataRDD.sample(false, 0.5)
    val value02: RDD[Int]  = dataRDD.sample(true, 2)
    dataRDD.collect().foreach(data => println(s"dataRDD: ${data}"))
    value01.collect().foreach(data => println(s"value01: ${data}"))
    value02.collect().foreach(data => println(s"value02: ${data}"))
  }


  /**
   * distinct 去重, 汇总然后去重，进行了shuffle，并且我可以指定去重后的分区数
   * groupBy 分组去重, 也是进行了shuffle
   *
   */
  test("test distinct") {
    val dataRDD = spark.makeRDD(List(1, 2, 3, 4, 1, 2), 2)
    // (0,1) (0,2) (0,3) (1,4) (1,1) (1,2)
    dataRDD.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    val value: RDD[Int] = dataRDD.distinct()
    // 1 2 3 4
    value.collect().foreach(println)
    // (0,4) (0,2) (1,1) (1,3)
    value.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    // 2
    println(value.getNumPartitions)

    // 根据groupBy去重
    val groupRdd: RDD[(Int, Iterable[Int])] = dataRDD.groupBy(data => data)
    val mapRdd: RDD[Int] = groupRdd.map(_._1)
    // (0,4) (0,2) (1,1) (1,3)
    mapRdd.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
  }


  /**
   * intersection: 交集
   * union: 并集
   * subtract: 差集
   * zip： 拉链操作, 元素个数必须一致，分区数必须一致
   */
  test("test intersection & union & subtract & zip") {
    val dataRDD01: RDD[Int] = spark.makeRDD(List(5, 1, 3, 4), 2)
    val dataRDD02: RDD[Int] = spark.makeRDD(List(1, 2, 3, 4, 7), 3)
    val dataRDD03: RDD[String] = spark.makeRDD(List("hello", "tom", "hello", "tom"), 2)

    val value01: RDD[Int] = dataRDD01.intersection(dataRDD02)
    val value02: RDD[Int] = dataRDD01.union(dataRDD02)
    val value03: RDD[Int] = dataRDD01.subtract(dataRDD02)
    val value04: RDD[(Int, Int)] = dataRDD01.zip(dataRDD02)
    val value05: RDD[(Int, String)] = dataRDD01.zip(dataRDD03)

    // 3 4 1
    value01.collect().foreach(data => println(s"value01: ${data}"))
    // 5 1 3 4 1 2 3 4 7
    value02.collect().foreach(data => println(s"value02: ${data}"))
    // 5
    value03.collect().foreach(data => println(s"value03: ${data}"))
    // 报错 元素个数不同
    // value04.collect().foreach(data => println(s"value04: ${data}"))
    // value05: (5,hello) value05: (1,tom) value05: (3,hello) value05: (4,tom)
    value05.collect().foreach(data => println(s"value05: ${data}"))

    println(value01.getNumPartitions) // 3
    println(value02.getNumPartitions) // 5
    println(value03.getNumPartitions) // 2
    println(dataRDD02.intersection(dataRDD01).getNumPartitions) // 3
  }

  /**
   * coalesce: 缩减分区，默认不会进行shuffle操作，
   * repartition：增加分区，默认会进行shuffle操作，底层调用的是coalesce
   *  俩者都可以增大或者所见分区，都可以进行shuffle操作，通过参数控制即可
   *
   */
  test("test coalesce & repartition") {
    val dataRDD = spark.makeRDD(List(1, 2, 3, 4, 1, 2), 2)
    println(dataRDD.repartition(4).getNumPartitions)  // 4
    println(dataRDD.coalesce(1).getNumPartitions)  // 1
  }

  /**
   * sortBy: 看了下源码，源码中类似于现根据map组成(k, v)对，然后根据sortKeyBy排序
   *    所以sortBy是会进行shuffle的，分区数保持不变，当然也可以更改，默认升序
   *
   */
  test("test sortBy") {
    val dataRDD = spark.makeRDD(List(5, 1, 3, 4), 2)
    // (0,5) (0,1) (1,3) (1,4)
    dataRDD.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    val value: RDD[Int] = dataRDD.sortBy(ele => ele)
    // 1 3 4 5
    value.collect().foreach(println)
    // 2
    println(value.getNumPartitions)
    // (0,1) (0,3) (1,4) (1,5)
    value.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
  }

  /**
   * sortByKey:
   *  K必须实现Ordered接口(特质)，返回一个按照key进行排序的
   */
  test("test sortByKey") {
    val dataRDD: RDD[(String, Int)] = spark.makeRDD(Array(("jack", 18),("tom", 19),("jack", 20)),3)
    // (0,(jack,18)) (1,(tom,19)) (2,(jack,20))
    dataRDD.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    val value: RDD[(String, Int)] = dataRDD.sortByKey()
    // (0,(jack,18)) (0,(jack,20)) (1,(tom,19))
    value.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    // 3
    println(value.getNumPartitions)
  }

  /**
   * partitionBy:
   *  将数据按照指定Partitioner重新进行分区。Spark默认的分区器是HashPartitioner
   *  分区器是否一样 分区数是否一致 一样或者一致直接返回
   */
  test("test partitionBy") {
    val dataRDD: RDD[(Int, String)] = spark.makeRDD(Array((1,"jack"),(2,"tom"),(3,"rose")),3)
    val value: RDD[(Int, String)] = dataRDD.partitionBy(new HashPartitioner(2))
    // 2
    println(value.getNumPartitions)
  }

  /**
   * reduceByKey:
   *  根据key进行聚合
   */
  test("test reduceByKey") {
    val dataRDD: RDD[(String, Int)] = spark.makeRDD(Array(("jack", 18),("tom", 19),("jack", 20)),3)
    val value: RDD[(String, Int)] = dataRDD.reduceByKey(_ + _)
    // (tom,19) (jack,38)
    value.collect().foreach(println)
  }

  /**
   * groupByKey:
   *  将数据源的数据根据key对value进行分组
   * groupByKey vs reduceByKey
   *    reduceByKey和groupByKey都存在shuffle的操作，但是reduceByKey可以在shuffle前对分区内相同key的数据进行预聚合（combine）功能，
   *      这样会减少落盘的数据量，而groupByKey只是进行分组，不存在数据量减少的问题，reduceByKey性能比较高。
   *    reduceByKey其实包含分组和聚合的功能。groupByKey只能分组，不能聚合，所以在分组聚合的场合下，推荐使用reduceByKey，
   *      如果仅仅是分组而不需要聚合。那么还是只能使用groupByKey
   * groupBy vs groupByKey
   *  groupBy 会将(key, value)放入迭代器，groupByKey只会将value放入迭代器
   */
  test("test groupByKey") {
    val dataRDD: RDD[(String, Int)] = spark.makeRDD(Array(("jack", 18),("tom", 19),("jack", 20)),3)
    val value: RDD[(String, Iterable[Int])] = dataRDD.groupByKey()
    val value1: RDD[((String, Int), Iterable[(String, Int)])] = dataRDD.groupBy(data => data)
    val value2: RDD[(String, Iterable[(String, Int)])] = dataRDD.groupBy(data => data._1)
    // (tom,CompactBuffer(19)) (jack,CompactBuffer(18, 20))
    value.collect().foreach(println)
    // ((jack,20),CompactBuffer((jack,20))) ((tom,19),CompactBuffer((tom,19))) ((jack,18),CompactBuffer((jack,18)))
    value1.collect().foreach(println)
    // (tom,CompactBuffer((tom,19))) (jack,CompactBuffer((jack,18), (jack,20)))
    value2.collect().foreach(println)
  }

  /**
   * aggregateByKey:
   *  将数据根据不同的规则进行分区内计算和分区间计算,
   *  初始值2每个分区都会进行计算
   *  第一个参数列表中的参数表示初始值
   *  第二个参数列表中含有两个参数
   *    第一个参数表示分区内的计算规则
   *    第二个参数表示分区间的计算规则
   */
  test("test aggregateByKey") {
    val dataRDD: RDD[(String, Int)] = spark.makeRDD(Array(("jack", 18),("tom", 19),("jack", 20)),3)
    // (0,(jack,18)) (1,(tom,19)) (2,(jack,20))
    dataRDD.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    val value: RDD[(String, Int)] = dataRDD.aggregateByKey(2)(_+_, _+_)
    // (0,(tom,21)) (1,(jack,42))
    value.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)

    // v 是可以改变类型的
    val value1: RDD[(String, (Int, Int))] = dataRDD.aggregateByKey((2, 0))(
      (u: (Int, Int), v) => (u._1 + v, u._2 + 1),
      (u1: (Int, Int), u2: (Int, Int)) => (u1._1 + u2._1, u1._2 + u2._2))
    // (0,(tom,(21,1))) (1,(jack,(42,2)))
    value1.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
  }

  /**
   * foldByKey:
   *  分区间和分区内的计算规则一致
   *  初始值2每个分区都会进行计算
   */
  test("test foldByKey") {
    val dataRDD: RDD[(String, Int)] = spark.makeRDD(Array(("jack", 18),("tom", 19),("jack", 20)),3)
    // (0,(jack,18)) (1,(tom,19)) (2,(jack,20))
    dataRDD.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    val value: RDD[(String, Int)] = dataRDD.foldByKey(2)(_+_)
    // (0,(tom,21)) (1,(jack,42))
    value.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
  }

  /**
   * combineByKey:
   *  第一个参数：分区内初始值的计算，其实我感觉就是list的第一个值，然后分区内的值会往这个list里面不断的追加
   *  第二个参数：分区内的计算
   *  第三个参数：分区间的计算
   *  combineByKey()允许用户返回值的类型与输入不一致。
   *
   *  groupByKey reduceByKey foldByKey aggregateByKey combineByKey 的不同
   *  相同：
   *    底层都是调用的combineByKeyWithClassTag
   *  groupByKey： 分组，不会在mapSide端进行合并元素
   *  reduceByKey: 聚合 无初始值 分区间分区内计算规则相同
   *  foldByKey： 聚合 有初始值(每个分区都会计算初始值) 分区间分区内计算规则相同
   *  aggregateByKey：聚合 有初始值(每个分区都会计算初始值) 分区间分区内计算规则可以不同
   *  combineByKey：聚合 初始值就是元素本身 分区间分区内计算规则可以不同
   *
   *  aggregateByKey vs combineByKey
   *  combineByKey： It does not provide combiner classtag information to the shuffle.
   *      combineByKeyWithClassTag(createCombiner, mergeValue, mergeCombiners,
   *        partitioner, mapSideCombine, serializer)(null)
   *  aggregateByKey:
   *    combineByKeyWithClassTag[U]((v: V) => cleanedSeqOp(createZero(), v),
   *      cleanedSeqOp, combOp, partitioner)
   *  俩者传递的partitioner  mapSideCombine serializer 都是默认的
   *  combineByKey 在combiner不提供classtag 信息 aggregateByKey 提供
   *
   *  combineByKeyWithClassTag[C](
        createCombiner: V => C,
        mergeValue: (C, V) => C,
        mergeCombiners: (C, C) => C,
        partitioner: Partitioner,
        mapSideCombine: Boolean = true,
        serializer: Serializer = null)(implicit ct: ClassTag[C])
   */
  test("test combineByKey") {
    val dataRDD: RDD[(String, Int)] = spark.makeRDD(Array(("jack", 18),("tom", 19),("jack", 20)),3)
    // (0,(jack,18)) (1,(tom,19)) (2,(jack,20))
    dataRDD.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    val value: RDD[(String, (Int, Int))] = dataRDD.combineByKey(
      (_, 1),
      (acc: (Int, Int), v) => (acc._1 + v, acc._2 + 1),
      (acc1: (Int, Int), acc2: (Int, Int)) => (acc1._1 + acc2._1, acc1._2 + acc2._2)
    )
    // (0,(tom,(19,1))) (1,(jack,(38,2)))
    value.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
  }

  /**
   * join:
   *  在类型为(K,V)和(K,W)的RDD上调用，返回一个相同key对应的所有元素连接在一起的(K,(V,W))的RDD
   *  rdd1 中的每个元素都会和 rdd2 中的每个元素 join， 比如 rdd1: (a, 1), (a, 2) rdd2: (a, 3) 会产生 (a, (1, 3)),  (a, (2, 3))
   * leftOuterJoin： 左连接
   * rightOuterJoin： 右连接
   * fullOuterJoin： 全连接
   *
   */
  test("test join") {
    val dataRDD01: RDD[(String, Int)] = spark.makeRDD(Array(("jack", 18),("tom", 19),("jack", 20), ("jerry", 21)),3)
    val dataRDD02: RDD[(String, String)] = spark.makeRDD(Array(("jack", "100kg"),("tom", "20kg"),("jack", "102kg"),("rose", "82kg")),4)
    // (0,(jack,18)) (1,(tom,19)) (2,(jack,20)) (2,(jerry,21))
    dataRDD01.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    // (0,(jack,100kg)) (1,(tom,20kg)) (2,(jack,102kg)) (3,(rose,82kg))
    dataRDD02.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    val value: RDD[(String, (Int, String))] = dataRDD01.join(dataRDD02)
    // (2,(tom,(19,20kg)))  (3,(jack,(18,100kg))) (3,(jack,(18,102kg))) (3,(jack,(20,100kg))) (3,(jack,(20,102kg)))
    value.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    // 4
    println(value.getNumPartitions)

    // (2,(tom,(19,Some(20kg)))) (2,(jerry,(21,None))) (3,(jack,(18,Some(100kg)))) (3,(jack,(18,Some(102kg))))
    // (3,(jack,(20,Some(100kg)))) (3,(jack,(20,Some(102kg))))
    val value1: RDD[(String, (Int, Option[String]))] = dataRDD01.leftOuterJoin(dataRDD02)
    // (2,(tom,(Some(19),20kg))) (3,(rose,(None,82kg))) (3,(jack,(Some(18),100kg))) (3,(jack,(Some(18),102kg)))
    // (3,(jack,(Some(20),100kg))) (3,(jack,(Some(20),102kg)))
    val value2: RDD[(String, (Option[Int], String))] = dataRDD01.rightOuterJoin(dataRDD02)
    // (2,(tom,(Some(19),Some(20kg)))) (2,(jerry,(Some(21),None))) (3,(rose,(None,Some(82kg))))
    // (3,(jack,(Some(18),Some(100kg)))) (3,(jack,(Some(18),Some(102kg)))) (3,(jack,(Some(20),Some(100kg)))) (3,(jack,(Some(20),Some(102kg))))
    val value3: RDD[(String, (Option[Int], Option[String]))] = dataRDD01.fullOuterJoin(dataRDD02)
    value1.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    value2.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    value3.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    println(value1.getNumPartitions)  // 4
    println(value2.getNumPartitions)  // 4
    println(value3.getNumPartitions)  // 4
  }


  /**
   * cogroup:
   *  在类型为(K,V)和(K,W)的RDD上调用，返回一个(K,(Iterable<V>,Iterable<W>))类型的RDD
   */
  test("test cogroup") {
    val dataRDD01: RDD[(String, Int)] = spark.makeRDD(Array(("jack", 18),("tom", 19),("jack", 20), ("jerry", 21)),3)
    val dataRDD02: RDD[(String, String)] = spark.makeRDD(Array(("jack", "100kg"),("tom", "20kg"),("jack", "102kg"),("rose", "82kg")),4)
    // (0,(jack,18)) (1,(tom,19)) (2,(jack,20)) (2,(jerry,21))
    dataRDD01.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    // (0,(jack,100kg)) (1,(tom,20kg)) (2,(jack,102kg)) (3,(rose,82kg))
    dataRDD02.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    val value: RDD[(String, (Iterable[Int], Iterable[String]))] = dataRDD01.cogroup(dataRDD02)
    // (2,(tom,(CompactBuffer(19),CompactBuffer(20kg))))  (2,(jerry,(CompactBuffer(21),CompactBuffer())))
    // (3,(rose,(CompactBuffer(),CompactBuffer(82kg)))) (3,(jack,(CompactBuffer(18, 20),CompactBuffer(100kg, 102kg))))
    value.mapPartitionsWithIndex((index, datas) => datas.map((index, _))).collect().foreach(println)
    // 4
    println(value.getNumPartitions)
  }
}
