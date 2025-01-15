package com.learn.java.jvm.chapter2;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class RuntimeConstantPoolOOM {

    /**
     * JDK6测试永久代：-XX:PermSize=6M -XX:MaxPermSize=6M
     * JD7和JDK8以上 字符串常量被存在java heap了 -Xms1m -Xmx1m
     */
    @Test
    public void test_perm() {
        // 使用Set保持着常量池引用，避免Full GC回收常量池行为
        Set<String> set = new HashSet<>();
        long i = 0;
        while (true) {
            set.add(String.valueOf(i++).intern());
        }
    }

    static class OOMObject {
    }

    /**
     * JDK7 以下测试方法区，JDK8以后永久代退出历史舞台了
     */
    @Test
    public void test_method_area() {
        while (true) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(OOMObject.class);
            enhancer.setUseCache(false);
            enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> proxy.invokeSuper(obj, args));
            enhancer.create();
        }
    }

    /**
     * String::intern()是一个本地方法，它的作用是如果字符串常量池中已经包含一个等于此String对象的
     * 字符串，则返回代表池中这个字符串的String对象的引用；否则，会将此String对象包含的字符串添加
     * 到常量池中，并且返回此String对象的引用。
     * JDK 6中运行，会得到两个false 在JDK 6中，intern()方法会把首次遇到的字符串实例复制到永久代的字符串常量池
     * 中存储，返回的也是永久代里面这个字符串实例的引用，而由StringBuilder创建的字符串对象实例在
     * Java堆上，所以必然不可能是同一个引用，结果将返回false。
     * JDK 7 以上，会得到一个ture一个false，在jdk7中，的intern()方法实现就不需要再拷贝字符串的实例
     * 到永久代了，既然字符串常量池已经移到Java堆中，那只需要在常量池里记录一下首次出现的实例引
     * 用即可，因此intern()返回的引用和由StringBuilder创建的那个字符串实例就是同一个。而对str2比较返
     * 回false，这是因为“java”这个字符串在执行String-Builder.toString()之前就已经出现过了，字符串常量
     * 池中已经有它的引用，不符合intern()方法要求“首次遇到”的原则，“计算机软件”这个字符串则是首次
     * 出现的，因此结果返回true。
     */
    @Test
    public void test_str_constant() {
        String str1 = new StringBuilder("计算机").append("软件").toString();
        System.out.println(str1.intern() == str1);
        String str2 = new StringBuilder("ja").append("va").toString();
        System.out.println(str2.intern() == str2);
    }
}
