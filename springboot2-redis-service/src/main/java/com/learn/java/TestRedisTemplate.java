package com.learn.java;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.hash.ObjectHashMapper;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@SpringBootTest
public class TestRedisTemplate {
    // 泛型必须使用Object
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Test
    public void test() {
        redisTemplate.opsForValue().set("name", "陈平安");
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println(name);

        stringRedisTemplate.opsForValue().set("name1", "陈平安1");
        String name1 = stringRedisTemplate.opsForValue().get("name1");
        System.out.println(name1);
    }

    @Data(staticConstructor = "of")
    @Accessors(chain = true)
    static class Person implements Serializable  {
        private String name;
        private int age;
        private Address address;
        private Date date;
        private LocalDateTime localDateTime;

        public Person() {
        }

        // 直接写一个默认的测试实例
        public static Person build() {
            return Person.of().setName("陈平安").setAge(14)
                    .setAddress(Address.of().setCity("骊珠小镇")
                            .setCountry("三十六小洞天")).setDate(new Date())
                    .setLocalDateTime(LocalDateTime.now());
        }
    }

    @Data(staticConstructor = "of")
    @Accessors(chain = true)
    static class Address implements Serializable {
        String city;
        String country;

        public Address() {
        }

        // 直接写一个默认的测试实例
        public static Address build() {
            return Address.of().setCity("骊珠小镇").setCountry("三十六小洞天");
        }

    }


    @Test
    public void test_hash_mapping() {
        Person person = Person.build();
        String key = "person:01";

        // 用这个感觉还不如用fastjson呢
        BeanUtilsHashMapper<Person> mapper = new BeanUtilsHashMapper<>(Person.class);
        Map<String, String> map = mapper.toHash(person);
        // TODO 生产环境慎用
        redisTemplate.delete(key);
        redisTemplate.opsForHash().putAll(key, map);
        Map<String, String> loadedHash = redisTemplate.<String, String>opsForHash().entries(key);
        Person entries = mapper.fromHash(Person.class, loadedHash);
        System.out.println(entries);

       /* Jackson2HashMapper mapper = new Jackson2HashMapper(false);
        Map<String, Object> map = mapper.toHash(person);
        redisTemplate.delete(key);
        redisTemplate.opsForHash().putAll(key, map);
        Map<String, Object> loadedHash = redisTemplate.<String, Object>opsForHash().entries(key);
        Person entity = mapper.fromHash(Person.class, loadedHash);
        System.out.println(entity);*/

        // 需要将RedisConfig的hash序列化注释掉
       /* HashMapper<Object, byte[], byte[]> mapper = new ObjectHashMapper();
        Map<byte[], byte[]> fromMapperHash = mapper.toHash(person);
        redisTemplate.delete(key);
        redisTemplate.opsForHash().putAll(key, fromMapperHash);
        Map<byte[], byte[]> loadedHash = redisTemplate.<byte[], byte[]>opsForHash().entries(key);
        Person result2 = (Person) mapper.fromHash(loadedHash);
        System.out.println(result2);*/
    }
}
