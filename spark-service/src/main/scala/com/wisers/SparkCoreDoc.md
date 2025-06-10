# spark doc spark组件原理以及yarn提交的简单介绍 
# spark 组件
    RDD任务切分中间分为：Application、Job、Stage和Task；
    Application：初始化一个SparkContext即生成一个Application；
    Job：一个Action算子就会生成一个Job；
    Stage：Stage等于宽依赖(ShuffleDependency)的个数加1；
    Task：一个Stage阶段中，最后一个RDD的分区个数就是Task的个数。
    注意：Application->Job->Stage->Task每一层都是1对n的关系。

    driver: Spark驱动器节点，用于执行Spark任务中的main方法，负责实际代码的执行工作。
            1 将用户程序转化为作业（job）
            2 在Executor之间调度任务(task)
            3 跟踪Executor的执行情况
            4 通过UI展示查询运行情况
            5 实际上，我们无法准确地描述Driver的定义，因为在整个的编程过程中没有看到任何有关Driver的字眼。所以简单理解，
                所谓的Driver就是驱使整个应用运行起来的程序，也称之为Driver类。
    executor: Spark Executor是集群中工作节点（Worker）中的一个JVM进程，负责在 Spark 作业中运行具体任务（Task），任务彼此之间相互独立。
            Spark 应用启动时，Executor节点被同时启动，并且始终伴随着整个 Spark 应用的生命周期而存在。如果有Executor节点发生了故障或崩溃，
            Spark 应用也可以继续执行，会将出错节点上的任务调度到其他Executor节点上继续运行。
            1 负责运行组成Spark应用的任务，并将结果返回给驱动器进程
            2 它们通过自身的块管理器（Block Manager）为用户程序中要求缓存的 RDD 提供内存式存储。RDD 是直接缓存在Executor进程内的，
                因此任务可以在运行时充分利用缓存数据加速运算。
            名称	                    说明
            --num-executors	        配置Executor的数量
            --executor-memory	    配置每个Executor的内存大小
            --executor-cores	    配置每个Executor的虚拟CPU core数量
    master & work: Spark集群的独立部署环境中，不需要依赖其他的资源调度框架，自身就实现了资源调度的功能，所以环境中还有其他两个核心组件：
            Master和Worker，这里的Master是一个进程，主要负责资源的调度和分配，并进行集群的监控等职责，类似于Yarn环境中的RM, 而Worker呢，
            也是进程，一个Worker运行在集群中的一台服务器上，由Master分配资源对数据进行并行的处理和计算，类似于Yarn环境中NM。
    applicationMaster: Hadoop用户向YARN集群提交应用程序时,提交程序中应该包含ApplicationMaster，用于向资源调度器申请执行任务
            的资源容器Container，运行用户自己的程序任务job，监控整个任务的执行，跟踪整个任务的状态，处理任务失败等异常情况。
            说的简单点就是，ResourceManager（资源）和Driver（计算）之间的解耦合靠的就是ApplicationMaster。
    并行度: 在分布式计算框架中一般都是多个任务同时执行，由于任务分布在不同的计算节点进行计算，所以能够真正地实现多任务并行执行，记住，这里是并行，
            而不是并发。这里我们将整个集群并行执行任务的数量称之为并行度。那么一个作业到底并行度是多少呢？这个取决于框架的默认配置。
            应用程序也可以在运行过程中动态修改。
    DAG:    这里所谓的有向无环图，并不是真正意义的图形，而是由Spark程序直接映射成的数据流的高级抽象模型。简单理解就是将整个程序计算的执行过程用
            图形表示出来,这样更直观，更便于理解，可以用于表示程序的拓扑结构。DAG（Directed Acyclic Graph）有向无环图是由点和线组成的拓扑图形，
            该图形具有方向，不会闭环。
# spark 原理
# spark on yarn
    Spark应用程序提交到Yarn环境中执行的时候，一般会有两种部署执行的方式：Client和Cluster。两种模式主要区别在于：Driver程序的运行节点位置。
    yarn client: 
        Client模式将用于监控和调度的Driver模块在客户端执行，而不是在Yarn中，所以一般用于测试。
        1 Driver在任务提交的本地机器上运行
        2 Driver启动后会和ResourceManager通讯申请启动ApplicationMaster
        3 ResourceManager分配container，在合适的NodeManager上启动ApplicationMaster，负责向ResourceManager申请Executor内存
        4 ResourceManager接到ApplicationMaster的资源申请后会分配container，然后ApplicationMaster在资源分配指定的NodeManager
            上启动Executor进程
        5 Executor进程启动后会向Driver反向注册，Executor全部注册完成后Driver开始执行main函数
        6 之后执行到Action算子时，触发一个Job，并根据宽依赖开始划分stage，每个stage生成对应的TaskSet，之后将task分发到各个Executor上执行。
    yarn cluster:
        Cluster模式将用于监控和调度的Driver模块启动在Yarn集群资源中执行。一般应用于实际生产环境。
        1 在YARN Cluster模式下，任务提交后会和ResourceManager通讯申请启动ApplicationMaster，
        2 随后ResourceManager分配container，在合适的NodeManager上启动ApplicationMaster，此时的ApplicationMaster就是Driver。
        3 Driver启动后向ResourceManager申请Executor内存，ResourceManager接到ApplicationMaster的资源申请后会分配container，
            然后在合适的NodeManager上启动Executor进程
        4 Executor进程启动后会向Driver反向注册，Executor全部注册完成后Driver开始执行main函数，
        5 之后执行到Action算子时，触发一个Job，并根据宽依赖开始划分stage，每个stage生成对应的TaskSet，之后将task分发到各个Executor上执行。

# 闭包检测
    算子以外的代码都是在Driver端执行, 算子里面的代码都是在Executor端执行。那么在scala的函数式编程中，就会导致算子内经常会用到算子外的数据，
    这样就形成了闭包的效果，如果使用的算子外的数据无法序列化，就意味着无法传值给Executor端执行，就会发生错误，所以需要在执行任务计算前，
    检测闭包内的对象是否可以进行序列化，这个操作我们称之为闭包检测。
# 序列化
    Java的序列化能够序列化任何的类。但是比较重（字节多），序列化后，对象的提交也比较大。Spark出于性能的考虑，
    Spark2.0开始支持另外一种Kryo序列化机制。Kryo速度是Serializable的10倍。当RDD在Shuffle数据的时候，简单数据类型、
    数组和字符串类型已经在Spark内部使用Kryo来序列化。
# RDD血缘关系
    RDD只支持粗粒度转换，即在大量记录上执行的单个操作。将创建RDD的一系列Lineage（血统）记录下来，以便恢复丢失的分区。RDD的Lineage
    会记录RDD的元数据信息和转换行为，当该RDD的部分分区数据丢失时，它可以根据这些信息来重新运算和恢复丢失的数据分区。
# RDD宽窄依赖
    窄依赖：窄依赖表示每一个父(上游)RDD的Partition最多被子（下游）RDD的一个Partition使用。
    宽依赖：宽依赖表示同一个父（上游）RDD的Partition被多个子（下游）RDD的Partition依赖，会引起Shuffle。

# 缓存
    cache & persist
        RDD通过Cache或者Persist方法将前面的计算结果缓存，默认情况下会把数据以缓存在JVM的堆内存中。但是并不是这两个方法被调用时立即缓存，
        而是触发后面的action算子时，该RDD将会被缓存在计算节点的内存中，并供后面重用。
    checkpoint
        所谓的检查点其实就是通过将RDD中间结果写入磁盘，由于血缘依赖过长会造成容错成本过高，这样就不如在中间阶段做检查点容错，
        如果检查点之后有节点出现问题，可以从检查点开始重做血缘，减少了开销。对RDD进行checkpoint操作并不会马上被执行，必须执行Action操作才能触发。
    cache vs checkpoint
        1 Cache缓存只是将数据保存起来，不切断血缘依赖。Checkpoint检查点切断血缘依赖。
        2 Cache缓存的数据通常存储在磁盘、内存等地方，可靠性低。Checkpoint的数据通常存储在HDFS等容错、高可用的文件系统，可靠性高。
        3 建议对checkpoint()的RDD使用Cache缓存，这样checkpoint的job只需从Cache缓存中读取数据即可，否则需要再从头计算一次RDD。
# RDD分区器
    Spark目前支持Hash分区和Range分区，和用户自定义分区。Hash分区为当前的默认分区。分区器直接决定了RDD中分区的个数、
    RDD中每条数据经过Shuffle后进入哪个分区，进而决定了Reduce的个数。

    只有Key-Value类型的RDD才有分区器，非Key-Value类型的RDD分区的值是None
    每个RDD的分区ID范围：0 ~ (numPartitions - 1)，决定这个值是属于那个分区的。

    Hash分区：对于给定的key，计算其hashCode,并除以分区个数取余
    Range分区：将一定范围内的数据映射到一个分区中，尽量保证每个分区数据均匀，而且分区间有序
