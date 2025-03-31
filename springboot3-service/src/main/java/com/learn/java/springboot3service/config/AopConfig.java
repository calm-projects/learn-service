package com.learn.java.springboot3service.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.util.Enumeration;

@Component
@Aspect
@Slf4j
public class AopConfig {
    @Pointcut("execution(* com.learn.java.springboot3service.controller.*.*(..))")
    public void serviceLayer() {
    }

    @Before("serviceLayer()")
    // @Before("execution(* com.learn.java.springboot3service.controller.*.*(..))")
    public void beforeAdvice(JoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        log.info("Before ... ");
        for (Object arg : args) {
            log.info("参数:{}", arg);
        }
        log.info("从request中获取.... ");
        // 也可以从request中获取
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.info("请求 URL: {}", request.getRequestURL().toString());
            log.info("请求方法: {}", request.getMethod());
        }
        log.info("Before ... ");
    }

    @After("serviceLayer()")
    public void afterAdvice(JoinPoint joinPoint) {
        log.info("After ...");
    }

    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void afterReturningAdvice(JoinPoint joinPoint, Object result) {
        log.info("After Returning advice: {} method executed successfully.", joinPoint.getSignature().getName());
        log.info("Returned result: {}", result);
    }

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void afterThrowingAdvice(JoinPoint joinPoint, Exception ex) {
        log.error("After Throwing advice: {} method threw an exception: {}", joinPoint.getSignature().getName(), ex.getMessage());
    }

    @Around("serviceLayer()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Around before ... ");
        // 执行目标方法
        Object proceed = joinPoint.proceed();
        log.info("Around after ... ");
        return proceed;
    }
}
