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
