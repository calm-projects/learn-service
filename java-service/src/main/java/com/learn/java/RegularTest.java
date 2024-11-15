package com.learn.java;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则测试
 */
@Slf4j
public class RegularTest {

    @Test
    public void test() {
        String target = "a\nb\nc";
        // 开始位置是a，表达式是b，直接不匹配  false
        testFlag(target, "^bc$", 0);
        // 开始位置是a b c， ^b匹配，结束位置也是a b c, c$匹配，但是bc没在一行不匹配换行符  false
        testFlag(target, "^bc$", Pattern.MULTILINE);
        // DOTALL 会开启.对\r \n的匹配，所以可以匹配  true
        testFlag(target, "^b.c$", Pattern.MULTILINE | Pattern.DOTALL);
        // .只能匹配一个，如果换行是\r\n的话需要写俩个. true
        testFlag("a\r\nb\r\nc", "^b..c", Pattern.MULTILINE | Pattern.DOTALL);
    }

    public static void testFlag(String target, String regex, int flag) {
        Pattern compile = Pattern.compile(regex, flag);
        Matcher matcher = compile.matcher(target);
        log.info("target:{}-->regex:{}; flag:{}, group:{}", target, regex, flag, matcher.find() ? matcher.group() : "不匹配");
    }
}