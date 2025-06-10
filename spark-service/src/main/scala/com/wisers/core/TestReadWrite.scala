package com.wisers.core

import com.wisers.UnitSpec
import org.apache.spark.rdd.RDD

/**
 * 各种读和写的操作
 * spark core 中介绍的读写操作比较较少 sql中较多
 *  所以spark core 可以使用操作数据湖的服务比如delta进行save
 *
 *  压缩格式	hadoop自带？	算法	文件扩展名	是否可切分	换成压缩格式后，原来的程序是否需要修改
    DEFLATE	是，直接使用	DEFLATE	.deflate	否	和文本处理一样，不需要修改
    Gzip	是，直接使用	DEFLATE	.gz	否	和文本处理一样，不需要修改
    bzip2	是，直接使用	bzip2	.bz2	是	和文本处理一样，不需要修改
    LZO	否，需要安装	LZO	.lzo	是	需要建索引，还需要指定输入格式
    Snappy	是，直接使用	Snappy	.snappy	否	和文本处理一样，不需要修改

    textfile: 存储方式为行式存储，在检索时磁盘开销大 数据解析开销大，而对压缩的text文件 hive无法进行合并和拆分
    SEQUENCEFILE: 二进制文件,以<key,value>的形式序列化到文件中,存储方式为行式存储，可以对文件进行分割和压缩，同时支持文件切割分片，
        提供了三种压缩方式：none,record,block（块级别压缩效率跟高）.默认是record(记录),基于行存储
    RCFILE: 存储方式为数据按行分块，每块按照列存储的行列混合模式，具有压缩快，列存取快的特点。在使用时，读记录尽量涉及到的block最少，
        这样读取需要的列只需要读取每个row group 的头部定义，具有明显速度优势。读取全量数据的操作 性能可能比sequencefile没有明显的优势。
    Orc:  Orc是从HIVE的原生格式RCFILE优化改进而来。
    Parquet: Parquet是Cloudera公司研发并开源的格式。
        这两者都属于列存储格式，但Orc严格上应该算是行列混合存储，首先根据行组分割整个表，在每一个行组内进行按列存储。
        Parquet文件和Orc文件都是是自解析的，文件中包括该文件的数据和元数据，Orc的元数据使用Protocol Buffers序列化。
        Parquet支持嵌套的数据模型，类似于Protocol Buffers，每一个数据模型的schema包含多个字段，每一个字段有三个属性：重复次数、数据类型和字段名。
        ORC原生是不支持嵌套数据格式的，而是通过对复杂数据类型特殊处理的方式实现嵌套格式的支持。
        两者都相比txt格式进行了数据压缩，相比而言，Orc的压缩比例更大，效果更好。
        计算引擎支持：都支持spark、MR计算引擎。
        查询引擎支持：Parquet被Spark SQL、Hive、Impala、Drill等支持，而Orc被Spark SQL、Presto、Hive支持，Orc不被Impala支持。
        ORC文件存储格式无论是在空间存储、导数据速度还是查询速度上表现的都较好一些，并且ORC可以一定程度上支持ACID操作，
        社区的发展目前也是Hive中比较提倡使用的一种列式存储格式
 */
class TestReadWrite extends UnitSpec{



  /**
   * read & save
   *  saveAsTextFile : textfile为默认格式，存储方式为行式存储，在检索时磁盘开销大 数据解析开销大，而对压缩的text文件 hive无法进行合并和拆分
   *  saveAsObjectFile:
   *      用于将RDD中的元素序列化成对象，存储到文件中。对于HDFS，默认采用SequenceFile保存。
   *  saveAsSequenceFile:
   *    二进制文件,以<key,value>的形式序列化到文件中,存储方式为行式存储，可以对文件进行分割和压缩，
   *    同时支持文件切割分片，提供了三种压缩方式：none,record,block（块级别压缩效率跟高）.默认是record(记录)
   *    基于行存储
   */
  test("test read") {

    // val dataRdd: RDD[(String, Int)] = spark.makeRDD(List(("a", 1), ("b", 1)), 2)
    val path: String = "spark-demo/src/main/scala/com/wisers/output/"
    val value1: RDD[String] = spark.textFile(path + "textFile")
    val value2: RDD[(String, Int)] = spark.sequenceFile[String, Int](path + "SequenceFile")
    val value3: RDD[(String, Int)] = spark.objectFile[(String, Int)](path + "objectFile")

    // 2
    value1.collect().foreach(println)
    // 2
    value2.collect().foreach(println)
    // 2
    value3.collect().foreach(println)

    println(value1.getNumPartitions)
    println(value2.getNumPartitions)
    println(value3.getNumPartitions)
  }

  test("test save") {
    val dataRdd: RDD[(String, Int)] = spark.makeRDD(List(("a", 1), ("b", 1)), 2)
    val path: String = "spark-demo/src/main/scala/com/wisers/output/"
    dataRdd.saveAsTextFile(path + "textFile")
    dataRdd.saveAsObjectFile(path + "objectFile")
    dataRdd.saveAsSequenceFile(path + "SequenceFile")
  }
}
