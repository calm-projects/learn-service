package com.learn.java.springboot3service.component;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CircularDeptB {
    // 使用@Autowired也存在循环依赖的问题
    /*private final CircularDeptA circularDeptA;

    public CircularDeptB(CircularDeptA circularDeptA) {
        this.circularDeptA = circularDeptA;
    }*/

    private final CircularDeptA circularDeptA;

    public CircularDeptB(@Lazy CircularDeptA circularDeptA) {
        this.circularDeptA = circularDeptA;
    }
}
