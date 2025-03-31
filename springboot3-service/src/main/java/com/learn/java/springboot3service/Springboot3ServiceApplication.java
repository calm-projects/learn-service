package com.learn.java.springboot3service;

import com.learn.java.springboot3service.component.CircularDeptA;
import com.learn.java.springboot3service.component.CircularDeptB;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Springboot3ServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Springboot3ServiceApplication.class, args);
        ConfigurableEnvironment environment = context.getEnvironment();
        String name = environment.getProperty("name");
        System.out.println(name);

        ThreadPoolTaskExecutor applicationTaskExecutor = (ThreadPoolTaskExecutor) context.getBean("applicationTaskExecutor");
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) context.getBean("taskExecutor");
        ThreadPoolTaskScheduler taskScheduler = (ThreadPoolTaskScheduler) context.getBean("taskScheduler");
        System.out.println(111111);

        CircularDeptA circularDeptA = context.getBean(CircularDeptA.class);
        circularDeptA.idCircularDeptB();
        CircularDeptB circularDeptB = context.getBean(CircularDeptB.class);
        System.out.println(circularDeptB);
    }
}
