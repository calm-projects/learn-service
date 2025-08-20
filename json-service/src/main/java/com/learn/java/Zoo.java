package com.learn.java;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 参考文献：https://www.baeldung.com/jackson-annotations
 * JsonTypeInfo 一般涉及到多态的时候使用 就是接口的时候
 *  使用什么作为type字段的值
 *      use = JsonTypeInfo.Id.NAME, {"animal":{"type":"com.wisers.dog","name":"dog","barkVolume":0.0}}
 *      use = JsonTypeInfo.Id.CLASS {"animal":{"type":"com.wisers.Zoo$Dog","name":"dog","barkVolume":0.0}}
 *  将子类以什么形式展现
 *      include = JsonTypeInfo.As.PROPERTY, {"animal":{"type":"com.wisers.dog","name":"dog","barkVolume":0.0}}
 *      include = JsonTypeInfo.As.WRAPPER_ARRAY, {"animal":["dog",{"name":"dog","barkVolume":0.0}]}
 *  子类的对应的key名称，随便起名 也可以叫做type007
 *      property = "type"
 * JsonSubTypes 定义子类的名称，不要在这里面定义太难看
 * JsonTypeName 直接在子类定义和 JsonSubTypes 里面定义是一样的
 */

@Builder
public class Zoo {
    public Animal animal;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Dog.class, name = "dog"),
            @JsonSubTypes.Type(value = Cat.class, name = "cat")
    })
    @JsonTypeName("animal")
    @Data
    public static class Animal {
        public String name;
    }

    @JsonTypeName("dog")
    @Builder
    public static class Dog extends Animal {
        public double barkVolume;
        public String name;
    }

    @JsonTypeName("cat")
    @Builder
    public static class Cat extends Animal {
        boolean likesCream;
        public int lives;
    }

    public static void main(String[] args) throws JsonProcessingException {
        Zoo.Dog dog = Dog.builder().name("dog").build();
        Zoo zoo = new Zoo(dog);

        String result = new ObjectMapper().writeValueAsString(zoo);
        System.out.println(result);

        assertThat(result, containsString("type"));
        assertThat(result, containsString("dog"));

        Animal animal = new Animal();
        animal.setName("animal");
        Zoo zoo1 = new Zoo(animal);
        String res = new ObjectMapper().writeValueAsString(zoo1);
        System.out.println(res);
        assertThat(result, containsString("type"));
        assertThat(result, containsString("animal"));

    }
}