package com.learn.java.lambda;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

/**
 * Stream相关操作很简单，直接点开类查看即可
 */
public class StreamTest {
    @Test
    public void test() {
        // 左闭右开
        IntStream.range(1, 3).forEach(System.out::println);
        int reduce = IntStream.range(1, 3).reduce(0, Integer::sum);
        Assertions.assertEquals(3, reduce);
    }
}
