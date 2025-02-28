package com.learn.java.io;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathExample {
    public static void main(String[] args) {
        // 创建 Path 对象
        Path path = Paths.get("example.txt");
        // 获取绝对路径
        System.out.println("绝对路径: " + path.toAbsolutePath());
        // 获取父路径
        System.out.println("父路径: " + path.getParent());
        // 获取文件名
        System.out.println("文件名: " + path.getFileName());
    }
}
