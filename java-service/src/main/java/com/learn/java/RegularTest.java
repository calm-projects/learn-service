package com.learn.java;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则测试
 */
public class RegularTest {

    @Test
    public void test() {
        String target = "a\r\nb\r\nc";
        testFlag(target, "^bc$", 0);
        testFlag(target, "^bc$", Pattern.MULTILINE);
        testFlag(target, "^bc$", Pattern.MULTILINE | Pattern.DOTALL);
    }

    public static void testFlag(String target, String regex, int flag) {
        Pattern compile = Pattern.compile(regex, flag);
        Matcher matcher = compile.matcher(target);
        boolean b = matcher.find();
        System.out.println(b);
    }


}