package com.learn.java;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * 描述信息
 *
 * @create: 2022-08-09 19:55
 */
public class TestDemo {

    @Before
    public void before(){

    }

    @After
    public void after(){

    }

    @Test
    public void test_log(){
        Assert.assertEquals("错误消息", "预期的值", "实际的值");
    }
}