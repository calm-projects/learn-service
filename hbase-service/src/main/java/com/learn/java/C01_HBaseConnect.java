package com.learn.java;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
/**
 * 根据官方 API 介绍，HBase 的客户端连接由 ConnectionFactory 类来创建，用户使用完成
 * 之后需要手动关闭连接。同时连接是一个重量级的，推荐一个进程使用一个连接，对 HBase
 * 的命令通过连接中的两个属性 Admin 和 Table 来实现。
 *
 * create 'udp:unique_key_all_dct', {NAME => 'f1', COMPRESSION => 'Snappy', VERSIONS => 1},{NUMREGIONS => 3, SPLITALGO => 'HexStringSplit'}
 * kafka
 */
public class C01_HBaseConnect {
    public static void main(String[] args) throws IOException {
        // 1. 创建配置对象
        Configuration conf = new Configuration();
        // 2. 添加配置参数
        conf.set("hbase.zookeeper.quorum","hadoop102,hadoop103,hadoop104");
        // 3. 创建 hbase 的连接
        // 默认使用同步连接
        Connection connection =
                ConnectionFactory.createConnection(conf);
        // 可以使用异步连接
        // 主要影响后续的 DML 操作
        CompletableFuture<AsyncConnection> asyncConnection =
                ConnectionFactory.createAsyncConnection(conf);
        // 4. 使用连接
        System.out.println(connection);
        // 5. 关闭连接
        connection.close();
    }
}