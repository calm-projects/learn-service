package com.learn.java.lambda;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

@Slf4j
@Accessors(chain = true)
@Setter
@NoArgsConstructor(staticName = "of")
public class ReferenceTest {

    private String name;

    private static void staticMethod(String name) {
        log.info("staticMethod: {}", name);
    }

    private void instanceMethod(String name) {
        log.info("instanceMethod: {}", name);
    }

    private void objectMethod() {
        log.info("objectMethod: {}", name);
    }

    @AllArgsConstructor
    @ToString
    static class Cat {
        private String name;
    }

    @Test
    public void test() {
        // 静态方法
        Stream.of("Jack", "Bob", "Tom").forEach(ReferenceTest::staticMethod);
        // 实例方法
        ReferenceTest referenceTest = new ReferenceTest();
        Stream.of("Jack", "Bob", "Tom").forEach(referenceTest::instanceMethod);
        // 对象方法
        Stream.of(ReferenceTest.of().setName("Jack"), ReferenceTest.of().setName("Tom"))
                .forEach(ReferenceTest::objectMethod);
        // 构造方法 @AllArgsConstructor 会生成一个私有的有参构造方法,看下class就知道了
        Stream.of("Jack", "Bob", "Tom").map(Cat::new).forEach(System.out::println);
    }
}
