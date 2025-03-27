package com.learn.java;

import com.learn.java.config.MyConfig;
import com.learn.java.entity.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        String[] beanDefinitionNames = context.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
        }

        // 从容器中获取
        User user1 = context.getBean("user", User.class);
        User user2 = context.getBean("user", User.class);
        System.out.println(user1);
        // true
        System.out.println(user1 == user2);

        // 直接通过方法创建
        MyConfig myConfig = context.getBean(MyConfig.class);
        User userByMethod1 = myConfig.user();
        User userByMethod2 = myConfig.user();
        /*
            当 @Configuration(proxyBeanMethods = false) 的时候，这里是false，也就是通过方法创建不是单例了 输出内容: 组件：false
            如果为true 则通过方法创建依旧是单例的 输出内容: 组件：true
         */
        System.out.println("组件：" + (userByMethod1 == userByMethod2));

        User user = context.getBean("user2", User.class);
        System.out.println(user);

    }
}