# spark sql doc
# dataFrame 定义
    DataFrame与RDD的主要区别在于，前者带有schema元信息，即DataFrame所表示的二维表数据集的每一列都带有名称和类型。
    这使得Spark SQL得以洞察更多的结构信息，从而对藏于DataFrame背后的数据源以及作用于DataFrame之上的变换进行了针对性的优化，
    最终达到大幅提升运行时效率的目标。

# dataset 定义
    DataSet是强类型的。比如可以有DataSet[Car]，DataSet[Person]。
    DataFrame是DataSet的特列，DataFrame=DataSet[Row]所以可以通过as方法将DataFrame转换为DataSet。Row是一个类型，
    跟Car、Person这些的类型一样，所有的表结构信息都用Row来表示。获取数据时需要指定顺序

# 分区分桶(partitionBy, bucketBy)
    分区, 分桶
    分区
    分桶
    不分区不分桶
    