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
 */
public class C02_HBaseConnect02 {
    // 设置静态属性 hbase 连接
    public static Connection connection = null;
    static {
        // 创建 hbase 的连接
        try {
            // 使用配置文件的方法
            connection = ConnectionFactory.createConnection();
        } catch (IOException e) {
            System.out.println("连接获取失败");
            e.printStackTrace();
        }
    }

    /**
     * 连接关闭方法,用于进程关闭时调用
     * @throws IOException
     */
    public static void closeConnection() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }
}