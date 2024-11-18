package com.learn.java.lambda;

import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;


public class InnerClassTest {
    private final String name = "InnerClassTest";

    class InnerClassA {
        public final String name = "InnerClassA";

        public class InnerClassB {
            public void fun() {
                // 内部类默认含有外部类的引用
                System.out.println(name);  // 打印InnerClassA的属性
                System.out.println(InnerClassA.this.name);  // 打印InnerClassA的属性
                System.out.println(InnerClassTest.this.name);  // 打印InnerClassTest的属性
            }
        }
    }

    @NoArgsConstructor(staticName = "of")
    static class InnerClassC {
        public void fun() {
            System.out.println("InnerClassC");
        }
    }


    @Test
    public void test() {
        InnerClassTest innerClassTest = new InnerClassTest();
        InnerClassTest.InnerClassA innerClassA = innerClassTest.new InnerClassA();
        InnerClassTest.InnerClassA.InnerClassB innerClassB = innerClassA.new InnerClassB();
        innerClassB.fun();
        InnerClassTest.InnerClassC.of().fun();
    }
}
