package com.learn.java.lambda;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

public class FunctionTest {

    /**
     * function 测试, 放到内部控制作用域
     * 直接mock下java.util.function.Function
     */
    @FunctionalInterface
    public interface Function<T> {
        String apply(T t);
    }

    /***
     * function 测试, 放到内部控制作用域
     *
     */
    @FunctionalInterface
    interface FunctionWithExceptions<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    public String trimStr(Function<String> t, String str) {
        return t.apply(str);
    }

    @Test
    public void testBase() {
        // 匿名内部类，直接idea快捷键alt+enter可以快速生成lambda
        Stream.of(1, 2, 3, 4).map(new java.util.function.Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) {
                return integer * 10;
            }
        }).forEach(System.out::println);
        // lambda表达式
        Stream.of(1, 2, 3, 4).map(integer -> integer * 10).forEach(System.out::println);
    }

    @Test
    public void testFunction() {
        // String res = trimStr(s -> s.trim(), " abc ");
        String res = trimStr(String::trim, "  abc ");
        Assertions.assertEquals("abc", res);
    }
}
