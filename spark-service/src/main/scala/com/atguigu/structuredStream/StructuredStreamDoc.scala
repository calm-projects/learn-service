package com.atguigu.structuredStream

/**
 * 描述信息
 *
 * @create: 2023-03-26 00:51
 */
object StructuredStreamDoc {

  /**
   * subscribe 指定主题 "topic1,topic2"
   * subscribePattern  topic.*
   * startingOffsets """{"topic1":{"0":23,"1":-2},"topic2":{"0":-2}}"""
   * -2 == earliest -1  == latest
   * "earliest", "latest" (streaming only), or json string """ {"topicA":{"0":23,"1":-1},"topicB":{"0":-2}} """
   * endingOffsets  """{"topic1":{"0":50,"1":-1},"topic2":{"0":-1}}"""
   * latest or json string {"topicA":{"0":23,"1":-1},"topicB":{"0":-1}}
   * batch query
   * kafkaConsumer.pollTimeoutMs  120000
   * fetchOffset.numRetries	 3
   * fetchOffset.retryIntervalMs	10
   * maxOffsetsPerTrigger none  streaming
   * minOffsetsPerTrigger none  streaming
   * maxTriggerDelay 15m streaming
   * kafka.group.id none streaming and batch
   * includeHeaders false streaming and batch
   */

  /**
   * Complete：完全输出
   * Append：只是输出追加的值不能进行更新，不能agg
   * Update ： 只会输出更新的值，如果不是agg和append一样
   *
   * 在watermark下
   * complete不变 watermark 对它无作用还是会缓存所有结果 建议不要使用
   * update：还是只更新更改的值
   * append：可以进行聚合了，但是append只有窗口结束的时候才会输出这也符合它的定位 输出不能变更的数据
   *
   * .dropDuplicates("uid")  // 去重重复数据 uid 相同就是重复.  可以传递多个列
   *
   *
   * join 操作
   * 好像只支持append模式呢
   *  可以 streaming DataSet/DataFrame 和 streaming DataSet/DataFrame
   *  也可以 streaming DataSet/DataFrame 和  DataSet/DataFrame
   *   Spark 会自动维护两个流的状态, 以保障后续流入的数据能够和之前流入的数据发生 join 操作,
   *   但这会导致状态无限增长. 因此, 在对两个流进行 join 操作时, 依然可以用 watermark 机制来消除过期的状态, 避免状态无限增长.
   *  外链接必须使用watermark
   *
   *  文件sink根本不能用，会生成大量小文件
   *
   *  Once 只是输出一次
   *  ProcessingTime 微批次暑输出
   *  .trigger(Trigger.Continuous()) 持续输出 比ProcessingTime要快 但是不支持agg
   *
   *
   *  org.apache.spark.sql.catalyst.analysis.TimeWindowing 窗口生成规则
   *
   * 窗口长度/滑动步长 == 有多少个窗口 （overlapping 有多少个重叠的窗口）
   * maxNumOverlapping <- ceil(windowDuration / slideDuration)
   * 遍历每个窗口获取其上下边界
   * for (i <- 0 until maxNumOverlapping)
   * startTime其实就是个偏移量，（用当前传入的时间-0）/ 步长 换算为秒的话就是timestamp的秒值除步长 取整数
   * windowId <- ceil((timestamp - startTime) / slideDuration)
   * 开始时间不就是当前时间往前取 n个窗口
   * windowStart <- windowId * slideDuration + (i - maxNumOverlapping) * slideDuration + startTime
   * windowEnd <- windowStart + windowDuration
   * return windowStart, windowEnd
   * */

}
