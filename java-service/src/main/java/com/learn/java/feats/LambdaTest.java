package com.learn.java.feats;


import lombok.NoArgsConstructor;

interface Animal {
    void eat();
}

@NoArgsConstructor(staticName = "of")
class Cat implements Animal {

    @Override
    public void eat() {
        System.out.println("I am cat1");
    }
}

public class LambdaTest {

    @NoArgsConstructor(staticName = "of")
    static class Cat2 implements Animal {
        @Override
        public void eat() {
            System.out.println("I am cat2");
        }
    }

    public static void main(String[] args) {
        // 外部接口
        Cat.of().eat();
        // 静态内部类
        Cat2.of().eat();
        // 局部内部类
        @NoArgsConstructor(staticName = "of")
        class Cat3 implements Animal {
            @Override
            public void eat() {
                System.out.println("I am cat3");
            }
        }
        Cat3.of().eat();

        // 匿名内部类
        new Animal() {
            @Override
            public void eat() {
                System.out.println("I am cat4");
            }
        }.eat();

        // Lambda表达式
        Animal animal = () -> System.out.println("I am cat5");
        animal.eat();
    }
}
