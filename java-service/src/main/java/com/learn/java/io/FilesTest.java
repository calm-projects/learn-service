package com.learn.java.io;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.*;
import java.io.IOException;

@Slf4j
public class FilesTest {
    public static void main(String[] args) {
        Path source = Paths.get("source.txt");
        Path target = Paths.get("target.txt");
        try {
            // 复制文件
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("文件复制成功！");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
