package com.learn.java.springboot3service.component;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CircularDeptA {
    // 使用@Autowired也存在循环依赖的问题
    /*private final CircularDeptB circularDeptB;

    public CircularDeptA(CircularDeptB circularDeptB) {
        this.circularDeptB = circularDeptB;
    }*/

    private final CircularDeptB circularDeptB;

    public CircularDeptA(@Lazy CircularDeptB circularDeptB) {
        this.circularDeptB = circularDeptB;
    }

    public void idCircularDeptB() {
        System.out.println(circularDeptB);
    }
}
