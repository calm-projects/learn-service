package com.atguigu.sourceAnalysis

/**
 * 描述信息
 */
class StageAndTask {


  // 0 提交任务
  /**
   * master work driver executor
   * application job stage task 线程
   * spark on yarn cluster
   * 命令行执行submit操作，我们可以设置一些参数，比如num-executors executors-memory 每个executors内存大小 executors-cores 每个executors的core大小还有对外内存默认最小
   * sparkSubmit -> 解析参数获得sparkcofing 和 mainclass YarnClusterApplication ——> 然后获取一个yarn的代理，创建AM 然后将application提交到了ApplicationMaster，
   * ，那就看applicationMaster的main方法，然后会执行rundriver，startUserApplication这时候会获得args --class main 反射 invoke执行获得spark context，然后driver 向AM
   * 注册，然后向rm申请资源创建容器executor，（容器创建一般都是bin/java xx executor是 YarnCoarseGrainedExecutorBackend ）看YarnCoarseGrainedExecutorBackend mian方法
   * 里面有一个setupEndpoint，这个会调用rpc的 onstart方法也就是 CoarseGrainedExecutorBackend.onstart, 接受的rpc就是spark Context里面的SchedulerBackend 实现类 CoarseGrainedSchedulerBackend ，看他的receiveAndReply回复一个ok，然后在看 receive 的 new Executor 这里就创建了一个executor
   *
   * 然后就开始执行我们自己写的类了，只有当action的时候才会触发执行，也就是上面都是懒加载的方式，只有action算子才会执行代码，点击collection
   * 然后就可以看到 dagScheduler.runJob
   *
   * Driver线程主要是初始化SparkContext对象，准备运行所需的上下文，然后一方面保持与ApplicationMaster的RPC连接，通过ApplicationMaster申请资源，
   * 另一方面根据用户业务逻辑开始调度任务，将任务下发到已有的空闲Executor上。当ResourceManager向ApplicationMaster返回Container资源时，ApplicationMaster
   * 就尝试在对应的Container上启动Executor进程，Executor进程起来后，会向Driver反向注册，注册成功后保持与Driver的心跳，同时等待Driver分发任务，
   * 当分发的任务执行完毕后，将任务状态上报给Driver。
   */

  // 1.0 查看stage划分
  /**
   * 1.0 随便找个action算子
   *
   * 2.0 查看RDD.runJob
   * runJob
   * --->一直点击
   * SparkContext.scala
   * dagScheduler.runJob(rdd, cleanedFunc, partitions, callSite, resultHandler, localProperties.get)
   * --->
   * val waiter = submitJob(rdd, func, partitions, callSite, resultHandler, properties)
   * --->
   * DAGScheduler.scala
   * 看最后一行有一个JobSubmitted，
   * eventProcessLoop.post(JobSubmitted(jobId, rdd, func2, partitions.toArray, callSite, waiter, Utils.cloneProperties(properties)))
   *    看post方法其实是一个线程，看run方法，最后还是调用的eventProcessLoop对象看他的方法DAGSchedulerEventProcessLoop
   *    看doReceive JobSubmitted 最后就到了下面hadleJobSubmitted
   *
   * 3.0 判断是shuffleStage 还是 resultStage
   * handleJobSubmitted
   * 里面有一个创建stage的方法
   * finalStage = createResultStage(finalRDD, func, partitions, jobId, callSite)
   *
   * // 创建stage
   * private def createResultStage(
   * rdd: RDD[_],
   * func: (TaskContext, Iterator[_]) => _,
   * partitions: Array[Int],
   * jobId: Int,
   * callSite: CallSite): ResultStage = {
   * checkBarrierStageWithDynamicAllocation(rdd)
   * checkBarrierStageWithNumSlots(rdd)
   * checkBarrierStageWithRDDChainPattern(rdd, partitions.toSet.size)
   * // 获取之前的stage
   * val parents = getOrCreateParentStages(rdd, jobId)
   * val id = nextStageId.getAndIncrement()
   * val stage = new ResultStage(id, rdd, func, partitions, parents, jobId, callSite)
   * stageIdToStage(id) = stage
   * updateJobIdStageIdMaps(jobId, stage)
   * stage
   * }
   *
   * // 判断是否是 shuffle 是的话分装到hash返回
   *
   * private[scheduler] def getShuffleDependencies(
   * rdd: RDD[_]): HashSet[ShuffleDependency[_, _, _]] = {
   * val parents = new HashSet[ShuffleDependency[_, _, _]]
   * val visited = new HashSet[RDD[_]]
   * val waitingForVisit = new ListBuffer[RDD[_]]
   * // 将调用算子的rdd添加到list集合
   * waitingForVisit += rdd
   * // 判断集合是否为空肯定不为空啊 刚添加的
   * while (waitingForVisit.nonEmpty) {
   * val toVisit = waitingForVisit.remove(0)
   * // 判断hash里面是否存在不存在则添加
   * if (!visited(toVisit)) {
   * visited += toVisit
   * toVisit.dependencies.foreach {
   * case shuffleDep: ShuffleDependency[_, _, _] =>
   * parents += shuffleDep
   * case dependency =>
   * waitingForVisit.prepend(dependency.rdd)
   * }
   * }
   * }
   * // 返回 shuffle
   * parents
   * }
   *
   * 4.0 这里我们进行遍历 hashset getOrCreateShuffleMapStage 获得并且创建stage
   * private def getOrCreateParentStages(rdd: RDD[_], firstJobId: Int): List[Stage] = {
   * getShuffleDependencies(rdd).map { shuffleDep =>
   * getOrCreateShuffleMapStage(shuffleDep, firstJobId)
   * }.toList
   * }
   */



   // 2.0 查看task划分
   /**
   * 为什么stage的最后一个  partitions 是任务的总数呢？？？？
   * 1.0 上面handleJobSubmitted方法的最后一行
   * private def submitStage(stage: Stage): Unit = {
   * val jobId = activeJobForStage(stage)
   * if (jobId.isDefined) {
   * logDebug(s"submitStage($stage (name=${stage.name};" +
   * s"jobs=${stage.jobIds.toSeq.sorted.mkString(",")}))")
   * if (!waitingStages(stage) && !runningStages(stage) && !failedStages(stage)) {
   * val missing = getMissingParentStages(stage).sortBy(_.id)
   * logDebug("missing: " + missing)
   * if (missing.isEmpty) {
   * logInfo("Submitting " + stage + " (" + stage.rdd + "), which has no missing parents")
   * submitMissingTasks(stage, jobId.get)
   * } else {
   * for (parent <- missing) {
   * submitStage(parent)
   * }
   * waitingStages += stage
   * }
   * }
   * } else {
   * abortStage(stage, "No active job for stage " + stage.id, None)
   * }
   * }
   *
   *
   * 2.0  submitMissingTasks(stage, jobId.get)
   *
   * 3.0 根据 submitMissingTasks 往下找 task val tasks: Seq[Task[_]] = try {
   * 可以看到代码
   * val tasks: Seq[Task[_]] = try {
   * val serializedTaskMetrics = closureSerializer.serialize(stage.latestInfo.taskMetrics).array()
   * stage match {
   * case stage: ShuffleMapStage =>
   * stage.pendingPartitions.clear()
   * partitionsToCompute.map { id =>
   * val locs = taskIdToLocations(id)
   * val part = partitions(id)
   * stage.pendingPartitions += id
   * new ShuffleMapTask(stage.id, stage.latestInfo.attemptNumber,
   * taskBinary, part, locs, properties, serializedTaskMetrics, Option(jobId),
   * Option(sc.applicationId), sc.applicationAttemptId, stage.rdd.isBarrier())
   * }
   *
   * case stage: ResultStage =>
   * partitionsToCompute.map { id =>
   * val p: Int = stage.partitions(id)
   * val part = partitions(p)
   * val locs = taskIdToLocations(id)
   * new ResultTask(stage.id, stage.latestInfo.attemptNumber,
   * taskBinary, part, locs, id, properties, serializedTaskMetrics,
   * Option(jobId), Option(sc.applicationId), sc.applicationAttemptId,
   * stage.rdd.isBarrier())
   * }
   * }
   *
   *
   * 4.0 点击 partitionsToCompute
   * val partitionsToCompute: Seq[Int] = stage.findMissingPartitions()
   *
   *
   * 5.0 查看器findMissingpartitions 实现类 有俩个 ResultStage 和 ShuffleMapStage
   * override def findMissingPartitions(): Seq[Int] = {
   * val job = activeJob.get
   * (0 until job.numPartitions).filter(id => !job.finished(id))
   * }
    *
    * 6.0 开始提交任务
    *  TaskScheduler.submitTasks
    * 7.0 createTaskSetManager 然后可以看到它把任务放到了 set集合中然后封装到了 TaskSetManager 里面
    *   看最后一行 backend.reviveOffers() 接受并获取任务 其实就是发送到了 CoarseGrainedSchedulerBackend
    *
    * 8.0 看 他的makeOffers这个方法 开始组织任务 scheduler.resourceOffers(workOffers) 调度策略了 本地优先了 就是数据在那个节点就把任务分到那个节点
    *
    * 9.0 然后 executorData.executorEndpoint.send(LaunchTask(new SerializableBuffer(serializedTask)))
    *
    * 10.0 这个rpc就是引用的executorEndpoint 也就是 CoarseGrainedExecutorBackend 看他的receive
    *
    * 11.0 executor.launchTask(this, taskDesc) 从线程池中启动一个任务
   * */

  // 3.0 shuffle 源码分析 hashShuffle 和 hash分区是不同的 一个是进行shuffle写磁盘的方式 一个是分区 shuffle写磁盘包括hash分区
  /**
   * 1.0
   * 上面我们分析了stage的划分，那stage是由DAGScheduler划分的那我们直接看他的方法 submitMissingTasks
   * shuffleMapStage
   *
   * 2.0
   * 看 shuffleMapTask.runTask方法 dep.shuffleWriterProcessor.write(rdd, dep, mapId, context, partition)
   *
   * 3.0
   * 这里有一个shuffleManager 里面有一个实现类SortShuffleManager 其实就是返回下面那个shuffle实体类的
   * 然后我们看到有三个实现类 UnsafeShuffleWriter SortShuffleWriter BypassMergeSortShuffleWriter
   * BypassMergeSortShuffleWriter 判断条件 mapSide不需要合并 并且 reduce task <= 200 为什么设置200 这个和他的原理相关
   *  因为他是用过去的hashShuffle类似，
   *  BypassMergeShuffle -> BufferedOutPutStream(缓冲)磁盘 -->默认一个task一个文件-->对文件进行merge 形成一个文件 index 和 data文件
   * UnsafeShuffleWriter
   *  可序列话的shuffle 满足条件是 kyro mapSide不需要合并 并且 下游分区数16777216 1<<24-1
   *
   * SortShuffleWriter 基本的
   * baseShuffleHandle reduceBykey -> [map| array] -> 排序 -> 达到阀值Batch(1000) -> BufferedOutPutStream(缓冲)磁盘
   * -->默认一个task一个文件-->对文件进行merge 形成一个文件 index
   *
   *  写的话看这俩个方法
   *   sorter.writePartitionedMapOutput(dep.shuffleId, mapId, mapOutputWriter) 将内存和磁盘的数据进行合并
   *   val partitionLengths = mapOutputWriter.commitAllPartitions()
   * 4.0
   *  那么读 一定是在 ResultStage rdd.iterator(partition, context) 最后就是看一个实现类 ShuffledRDD的方法
   */

}
