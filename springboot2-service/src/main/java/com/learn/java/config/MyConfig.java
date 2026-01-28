package com.learn.java.config;

import com.learn.java.entity.Animal;
import com.learn.java.entity.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/*
proxyBeanMethods :
        指定@Bean方法是否应该被代理以强制执行bean生命周期行为，即使在用户代码中直接调用@Bean方法的情况下，也可以返回共享的单例bean实例。
    此特性需要方法拦截，通过运行时生成的CGLIB子类实现，该子类具有限制，例如配置类及其方法不允许声明final。
        默认值为true，允许通过配置类内的`直接方法调用`以及外部调用该配置的@Bean方法进行“bean间引用”。如果不需要这样做，因为每个特定配置
    的@Bean方法都是自包含的(“自包含”指的是这些方法不依赖于其他通过@Bean定义的Spring管理的bean，或者它们不依赖于Spring配置类所提供的特定
    上下文或状态。比如下面的user不依赖Animal)，并且设计为容器使用的普通工厂方法，请将此标志切换为false，以避免CGLIB子类处理。
### Full模式（proxyBeanMethods = true）
Spring容器启动->创建AppConfig的CGLIB代理类-> 拦截所有@Bean方法调用-> 检查容器中是否已有该Bean-> 有返回容器中的单例\无执行方法并注册到容器
### Lite模式（proxyBeanMethods = false）
Spring容器启动 ->  AppConfig就是普通Java类（无代理）-> @Bean方法直接执行 -> 方法间调用就是普通Java方法调用

只要从容器中获取俩个都是单例的，返回true
    System.out.println(context.getBean("user", User.class) == context.getBean("user", User.class));
通过「配置类实例直接调用 @Bean 方法」时,full是同一个对象， lite每次都new一个对象，full是通过代理从BeanFactory里面获取的，lite就是调用的方法
    代码1：
    MyConfig myConfig = context.getBean(MyConfig.class);
    User user1 = myConfig.user();
    User user2 = myConfig.user();
    代码2：其实本质还是方法调用，只要设计到方法调用一定会返回俩个不同的对象
    @Configuration(proxyBeanMethods = false)
    class BadConfig {
        @Bean
        A a() {
            return new A();
        }
        @Bean
        B b() {
            return new B(a()); // ❌ 会 new 两个 A
        }
    }
 为什么好多底层都写proxyBeanMethods = false? 不是为了快，而是为了确定性
    配置类本身就不需要 @Bean 之间的调用语义
    被设计成“无状态 Bean 工厂”（不依赖乱七八糟的东西）
    关闭代理可以减少启动期开销（但这是次要收益）
 */
@Configuration(proxyBeanMethods = false)
// 注入的是User(name=null)
// @Import(User.class)
// 当ConditionalTestConfig未注入的时候，MyConfig以及下面的bean都不会注入, @ConditionalOnBean 可以在配置类上使用也可以在@Bean位置使用
// @ConditionalOnBean(ConditionalTestConfig.class)
// 表示在类路径上只要存在这个类即可
@ConditionalOnClass(ConditionalTestConfig.class)
public class MyConfig {
    /**
     *  注入的是 User(name=tom)
     */
    @Bean
    public User user() {
        return User.of().setName("tom");
    }

    /**
     *  我可以注册多个，但是每一个的name是不同的， user() 和 user2() 方法分别创建不同的 User 实例。
     */
    @Bean
    public User user2() {
        return User.of().setName("jack");
    }

    @Bean
    public Animal animal() {
        return Animal.of().setName("cat");
    }
}
