package com.learn.java.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

interface MyInterface {
    void doSomething();
}

class MyInterfaceImpl implements MyInterface {
    @Override
    public void doSomething() {
        System.out.println("Doing something...");
    }
}

class MyInvocationHandler implements InvocationHandler {
    private final Object target;

    public MyInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before method call");
        Object result = method.invoke(target, args);
        System.out.println("After method call");
        return result;
    }
}

public class JdkDynamicProxyTest {
    // 这里不要使用@Test,如果使用请将下面的参数配置到VM options，加载顺序的问题，
    // -Djdk.proxy.ProxyGenerator.saveGeneratedFiles=true
    public static void main(String[] args) {
        MyInterface target = new MyInterfaceImpl();
        // jdk会在内存中生成代理类的字节码，下面这个配置可以将生成的字节码保存到磁盘上
        // jdk8配置，可以查看ProxyGenerator里面的属性saveGeneratedFiles配置的是什么
        System.setProperty("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
        // jdk8以上配置
        System.setProperty("jdk.proxy.ProxyGenerator.saveGeneratedFiles", "true");
        MyInterface proxy = (MyInterface) Proxy.newProxyInstance(
                MyInterface.class.getClassLoader(),
                new Class[]{MyInterface.class},
                new MyInvocationHandler(target)
        );
        proxy.doSomething();
    }
}
