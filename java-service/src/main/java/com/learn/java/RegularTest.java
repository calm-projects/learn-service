package com.learn.java;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则测试
 * matcher.group(int group) 的作用
 * matcher.group(int group) 是 Java 正则表达式 API 提供的方法，用于获取捕获组的内容。
 * 参数是一个整数，表示捕获组的编号：
 * group(0)：返回整个匹配的内容（即整个正则表达式匹配的部分）。
 * group(1)：返回第一个捕获组的内容（即第一个用括号 () 捕获的部分）。
 * group(2)：返回第二个捕获组的内容，依此类推。
 * $1 是在替换操作中使用的语法，表示引用第一个捕获组的内容。
 * 它只能在像 replaceAll() 或 replaceFirst() 这样的方法中使用，而不是在 matcher.group() 中使用。
 */
@Slf4j
public class RegularTest {

    @Test
    public void testBase() {
        String input = "abc, a1b1c1, a2b2c2";
        String regex = "b\\d";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        // 查找所有匹配项
        while (matcher.find()) {
            // 找到匹配项 'b1' 在位置: 7 找到匹配项 'b2' 在位置: 15
            System.out.println("找到匹配项 '" + matcher.group() + "' 在位置: " + matcher.start());
        }
    }


    /**
     * java 中忽略大小写可以使用(?i),(?i)后面的内容都可以忽略大小写
     */
    @Test
    public void testCaseInsensitive() {
        Pattern compile = Pattern.compile("AAA (?i)Country Name");
        // group:不匹配
        Matcher matcher = compile.matcher("aaa country name");
        log.info("group:{}", matcher.find() ? matcher.group() : "不匹配");
        Matcher matcher2 = compile.matcher("AAA COUNTRY name");
        //  group:AAA COUNTRY name
        log.info("group:{}", matcher2.find() ? matcher2.group() : "不匹配");
    }

    /**
     * java 多行匹配可以使用(?m)
     */
    @Test
    public void testMultiple() {
        String target = "a1\nb1\nc1";
        // 不匹配
        this.testCommon(target, "^b1");
        // 匹配b1 flags对应的表达式不能随意放置，最好放置在表达式开端
        this.testCommon(target, "(?m)^b1");
        // 不匹配
        this.testCommon(target, "^b1(?m)");
        // 不匹配
        this.testCommon(target, "^b(?m)1");
    }

    private void testCommon(String target, String regex) {
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(target);
        log.info("target:{}-->regex:{}; group:{}", target, regex, matcher.find() ? matcher.group() : "不匹配");
    }


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
        testFlag("abc, a1b1c1, a2b2c2", "b", 0);
    }

    public static void testFlag(String target, String regex, int flag) {
        Pattern compile = Pattern.compile(regex, flag);
        Matcher matcher = compile.matcher(target);
        log.info("target:{}-->regex:{}; flag:{}, group:{}", target, regex, flag, matcher.find() ? matcher.group() : "不匹配");
    }

    /**
     *  (pattern)  (?:pattern) (?=pattern) (?!pattern)
     *  matcher.group(int group) 的作用
     *  matcher.group(int group) 是 Java 正则表达式 API 提供的方法，用于获取捕获组的内容。
     *  参数是一个整数，表示捕获组的编号：
     *  group(0)：返回整个匹配的内容（即整个正则表达式匹配的部分）。
     *  group(1)：返回第一个捕获组的内容（即第一个用括号 () 捕获的部分）。
     *  group(2)：返回第二个捕获组的内容，依此类推。
     *  $1 是在替换操作中使用的语法，表示引用第一个捕获组的内容。
     *  它只能在像 replaceAll() 或 replaceFirst() 这样的方法中使用，而不是在 matcher.group() 中使用。
     */
    @Test
    void test_pattern(){
        // (pattern)：捕获匹配的子表达式, 匹配 pattern 并捕获该匹配，供以后使用（例如通过 $0...$9 引用）。
        String input = "Today's date is 2023-10-05.";
        String regex = "(\\d{4})-(\\d{2})-(\\d{2})"; // 捕获年、月、日
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        /*
            完整匹配: 2023-10-05
            年: 2023
            月: 10
            日: 05
         */
        if (matcher.find()) {
            System.out.println("完整匹配: " + matcher.group(0)); // 完整匹配
            System.out.println("年: " + matcher.group(1));       // 第一个括号捕获的内容
            System.out.println("月: " + matcher.group(2));       // 第二个括号捕获的内容
            System.out.println("日: " + matcher.group(3));       // 第三个括号捕获的内容
        }

        // 使用 () 捕获子表达式。在正则表达式内部，通过 \1, \2, ... 引用捕获的内容。在替换操作中，通过 $1, $2, ... 引用捕获的内容。
        input = "123-123 456-456 789-123";
        regex = "(\\d+)-\\1"; // 匹配重复的数字
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(input);
        /*
        匹配到标签: 123-123
        匹配到标签: 123
        匹配到标签: 456-456
        匹配到标签: 456
         */
        while (matcher.find()) {
            System.out.println("匹配到标签: " + matcher.group()); // 匹配的内容
            System.out.println("匹配到标签: " + matcher.group(1)); // 引用捕获的标签名
        }
        String result = matcher.replaceAll("$1"); // 使用 $1 引用捕获的内容
        // 123 456 789-123
        System.out.println(result);



        // (?:pattern)：非捕获匹配, 匹配 pattern 但不捕获该匹配，适用于不需要引用匹配结果的情况。
        input = "The industry and industries are important.";
        regex = "industr(?:y|ies)"; // 非捕获匹配 y 或 ies
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(input);
        /*
        匹配到: industry
        匹配到: industries
         */
        while (matcher.find()) {
            System.out.println("匹配到: " + matcher.group());
        }

        // (?=pattern)：正向预测先行搜索, 匹配某个位置后紧跟指定模式 pattern 的内容，但不占用字符。
        input = "Windows 95, Windows NT, Windows 3.1";
        regex = "Windows (?=95|98|NT|2000)"; // 正向预测先行
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(input);
        /*
        匹配到: Windows
        匹配到: Windows
         */
        while (matcher.find()) {
            System.out.println("匹配到: " + matcher.group());
        }

        // (?!pattern)：反向预测先行搜索, 匹配某个位置后不紧跟指定模式 pattern 的内容，但不占用字符。
        input = "Windows 95, Windows NT, Windows 3.1";
        regex = "Windows (?!95|98|NT|2000)"; // 反向预测先行
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(input);
        // 这里只匹配了 Windows 3.1 中的 Windows，因为它后面不是 95、98、NT 或 2000。
        while (matcher.find()) {
            System.out.println("匹配到: " + matcher.group());
        }
    }
}