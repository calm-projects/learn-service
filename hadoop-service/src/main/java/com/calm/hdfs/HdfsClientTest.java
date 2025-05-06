package com.calm.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class HdfsClientTest {

    private static final String host = "hdfs://hadoop01:8020";
    private static final String replication = "2";
    private FileSystem fs;

    @Before
    public void init() throws URISyntaxException, IOException, InterruptedException {
        // hdfs-default.xml => hdfs-site.xml=> 在项目资源目录下的配置文件 =》代码里面的配置
        Configuration configuration = new Configuration();
        configuration.set("dfs.replication", replication);
        fs = FileSystem.get(new URI(host), configuration, "hadoop");
    }

    @After
    public void close() throws IOException {
        fs.close();
    }

    /**
     * 创建目录
     */
    @Test
    public void testMkdir() throws IOException {
        fs.mkdirs(new Path("/calm"));
    }

    /**
     * 从本地上传文件
     */
    @Test
    public void testPut() throws IOException {
        fs.copyFromLocalFile(false, true, new Path("D:\\bigdata\\wordCount.txt"), new Path("/calm"));
    }


    /**
     * 从内存中上传文件
     */
    @Test
    public void testPut2() throws IOException {
        FSDataOutputStream fos = fs.create(new Path("/calm/hello"));
        fos.write("hello world".getBytes());
    }

    @Test
    public void testGet() throws IOException {
        // useRawLocalFileSystem是否使用RawLocalFileSystem作为本地文件系统。RawLocalFileSystem是非校验和的，所以，它不会在本地创建任何crc文件。
        fs.copyToLocalFile(false, new Path("/calm/hello"), new Path("D:\\bigdata"), true);
        fs.copyToLocalFile(false, new Path("/calm/hello"), new Path("D:\\bigdata\\hello1"), false);
    }

    /**
     * 删除
     */
    @Test
    public void testRm() throws IOException {
        fs.delete(new Path("/calm/hello"), false);
        // 删除非空目录
        fs.delete(new Path("/calm"), true);
    }

    /**
     * mv 操作
     */
    @Test
    public void testMv() throws IOException {
        testPut2();
        fs.rename(new Path("/calm/hello"), new Path("/calm/hello.txt"));
    }

    /**
     * 获取文件详细信息
     */
    @Test
    public void fileDetail() throws IOException {
        // 获取所有文件信息
        RemoteIterator<LocatedFileStatus> listFiles = fs.listFiles(new Path("/"), true);
        while (listFiles.hasNext()) {
            LocatedFileStatus fileStatus = listFiles.next();
            System.out.println("==========" + fileStatus.getPath() + "=========");
            System.out.println(fileStatus.getPermission());
            System.out.println(fileStatus.getOwner());
            System.out.println(fileStatus.getGroup());
            System.out.println(fileStatus.getLen());
            System.out.println(fileStatus.getModificationTime());
            System.out.println(fileStatus.getReplication());
            System.out.println(fileStatus.getBlockSize());
            System.out.println(fileStatus.getPath().getName());

            // 获取块信息
            BlockLocation[] blockLocations = fileStatus.getBlockLocations();
            System.out.println(Arrays.toString(blockLocations));

        }
    }


    @Test
    public void testIsFile() throws IOException {
        FileStatus[] listStatus = fs.listStatus(new Path("/"));
        for (FileStatus status : listStatus) {
            if (status.isFile()) {
                System.out.println("文件：" + status.getPath().getName());
            } else {
                System.out.println("目录：" + status.getPath().getName());
            }
        }
    }
}
