package com.learn.java;

import com.learn.java.entity.User;
import com.learn.java.repo.UserRepository;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

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
    static class Person implements Serializable {
        private String name;
        private int age;
        private Address address;
        private Date date;
        private LocalDateTime localDateTime;

        // 添加of后lombok会将空参构造改为private,而且还不能通过lombok @NoArgsConstructor 添加一个公共的，直接自己写一个
        public Person() {
        }

        // 直接写一个默认的测试实例
        public static Person build() {
            return Person.of().setName("陈平安")
                    .setAge(14)
                    .setAddress(Address.build())
                    .setDate(new Date())
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

        // 无法读取静态内部类，用这个不如用jackson或者fastjson
       /* BeanUtilsHashMapper<Person> mapper = new BeanUtilsHashMapper<>(Person.class);
        Map<String, String> map = mapper.toHash(person);
        // TODO 生产环境慎用
        redisTemplate.delete(key);
        redisTemplate.opsForHash().putAll(key, map);
        Map<String, String> loadedHash = redisTemplate.<String, String>opsForHash().entries(key);
        Person entries = mapper.fromHash(Person.class, loadedHash);
        System.out.println(entries);*/

        Jackson2HashMapper mapper = new Jackson2HashMapper(false);
        Map<String, Object> map = mapper.toHash(person);
        redisTemplate.delete(key);
        redisTemplate.opsForHash().putAll(key, map);
        Map<String, Object> loadedHash = redisTemplate.<String, Object>opsForHash().entries(key);
        Person entity = mapper.fromHash(Person.class, loadedHash);
        System.out.println(entity);

        // 需要将RedisConfig的hash序列化注释掉
       /* HashMapper<Object, byte[], byte[]> mapper = new ObjectHashMapper();
        Map<byte[], byte[]> fromMapperHash = mapper.toHash(person);
        redisTemplate.delete(key);
        redisTemplate.opsForHash().putAll(key, fromMapperHash);
        Map<byte[], byte[]> loadedHash = redisTemplate.<byte[], byte[]>opsForHash().entries(key);
        Person result2 = (Person) mapper.fromHash(loadedHash);
        System.out.println(result2);*/
    }

    /**
     * 127.0.0.1:6379> lpush myList 1 2 3 4 5 6
     * 6
     */
    @Test
    void test_pipelining() {
        int batchSize = 2;
        List<Object> lists = stringRedisTemplate.executePipelined((RedisCallback<String>) connection -> {
            StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
            for (int i = 0; i < batchSize; i++) {
                stringRedisConn.rPop("myList");
            }
            return null;
        });
        lists.forEach(System.out::println);
    }

    /**
     * set name tom
     * It is ideal to configure a single instance of
     * DefaultRedisScript in your application context to avoid re-calculation of the script’s SHA1 on every script run.
     * 官网推荐注入bean的方式
     * ScriptSource scriptSource = new ResourceScriptSource(new ClassPathResource("META-INF/scripts/checkandset.lua"));
     * return RedisScript.of(scriptSource, Boolean.class);
     */
    @Test
    void test_script(){
        String script = "-- checkandset.lua\n" +
                "local current = redis.call('GET', KEYS[1])\n" +
                "if current == ARGV[1]\n" +
                "  then redis.call('SET', KEYS[1], ARGV[2])\n" +
                "  return true\n" +
                "end\n" +
                "return false";
        RedisScript<Boolean> redisScript = RedisScript.of(script, Boolean.class);
        Boolean execute = stringRedisTemplate.execute(redisScript, Collections.singletonList("name"), "tom", "jack");
        System.out.println(execute);
    }

    @Test
    void test_transactions() {
        List<Object> results = stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
            @SuppressWarnings("unchecked")
            public List<Object> execute(@NonNull RedisOperations operations) throws DataAccessException {
                try {
                    operations.multi();
                    operations.opsForSet().add("key", "value1");
                    operations.opsForSet().add("key", "value2");
                    return operations.exec();
                } catch (RuntimeException e) {
                    operations.discard();
                    throw e;
                }
            }
        });
        assert results != null;
        results.forEach(System.out::println);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void test_redis_repo() {
        User user = new User();
        user.setId("1");
        user.setUsername("tom");
        user.setPassword("123456");
        userRepository.save(user);
        Optional<User> result = userRepository.findById(user.getId());
        System.out.println(result.orElse(null));
        long count = userRepository.count();
        System.out.println(count);
    }
}
