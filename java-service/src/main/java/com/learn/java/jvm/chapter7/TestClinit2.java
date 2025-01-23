package com.learn.java.jvm.chapter7;

public class TestClinit2 {
    static class Parent {
        public static int A = 1;

        static {
            A = 2;
        }
    }

    static class Sub extends Parent {
        public static int B = A;
    }

    /**
     * 字段B的值将会是2而不是1
     */
    public static void main(String[] args) {
        System.out.println(Sub.B);
    }
}
