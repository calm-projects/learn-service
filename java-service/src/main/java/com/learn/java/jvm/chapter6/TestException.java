package com.learn.java.jvm.chapter6;

public class TestException {

    public static void main(String[] args) {
        TestException te = new TestException();
        System.out.println(te.inc());
    }

    public int inc() {
        int x;
        try {
            x = 1;
            return x;
        } catch (Exception e) {
            x = 2;
            return x;
        } finally {
            x = 3;
        }
    }
}
