package com.learn.java.reflect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射
 */
@Slf4j
public class ReflectTest {

    interface Animal {
        void m1();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Cat implements Animal {
        private String name;
        private Integer age;

        private Cat(String name) {
            this.name = name;
            this.age = 18;
        }

        @Override
        public void m1() {
            System.out.println(this);
        }
    }

    @Test
    public void testInstance() throws Exception {
        // 不推荐 Class.forName("") 会读取class文件，初始化类
        String className = "com.learn.java.reflect.ReflectTest";
        Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(className);
        @Deprecated
        ReflectTest reflectTest = (ReflectTest) aClass.newInstance();

        // 推荐使用
        Class<Cat> catClass = Cat.class;
        Constructor<Cat> constructor = catClass.getConstructor(String.class, Integer.class);
        Cat catInstance = constructor.newInstance("tom", 10);
        catInstance.m1();

        // 私有的构造方法
        catClass.getDeclaredConstructor(String.class).newInstance("tom").m1();
    }


    @Test
    public void testField() throws Exception {
        Class<Cat> aclass = Cat.class;
        Constructor<Cat> constructor = aclass.getConstructor(String.class, Integer.class);
        Cat cat = constructor.newInstance("tom", 10);

        Field field = aclass.getDeclaredField("name");
        field.setAccessible(true);
        field.set(cat, "jack");
        // ReflectTest.Cat(name=jack, age=10)
        System.out.println(cat);
    }

    @Test
    public void testMethod() throws Exception {
        Class<Cat> aclass = Cat.class;
        Constructor<Cat> constructor = aclass.getConstructor(String.class, Integer.class);
        Cat cat = constructor.newInstance("tom", 10);

        Method method = aclass.getDeclaredMethod("m1");
        method.setAccessible(true);
        method.invoke(cat);
    }

    @Test
    public void testOther() throws Exception {
        Class<Cat> aclass = Cat.class;
        log.info("类加载器对象:{}", aclass.getClassLoader());
        log.info("直接父类:{}", aclass.getGenericSuperclass());
        for (Class<?> c : aclass.getInterfaces()) {
            log.info("实现的接口:{}", c);
        }
        for (Annotation annotation : aclass.getAnnotations()) {
            log.info("添加的注解:{}", annotation);
        }
    }
}
