package com.learn.java.jvm.chapter6;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class TestClassStructureSub extends TestClassStructureBase {
    private static final String name1 = "JACK";
    private static final String name2 = "JACK";
    private static String name3 = "JACK";
    private final String name4 = "JACK";
    private final int age = 10;
    private float weight;

    @Test
    public void testAdd() {
        int i = 0;
        i = i++;
        Assertions.assertEquals(i, 0);
        int j = 0;
        j = ++j;
        Assertions.assertEquals(j, 1);
    }

    @Test
    public void testAdd2() {
        String str1 = new StringBuilder("计算机").append("软件").toString();
        System.out.println(str1.intern() == str1);
        String str2 = new StringBuilder("ja").append("va").toString();
        System.out.println(str2.intern() == str2);
    }
}
