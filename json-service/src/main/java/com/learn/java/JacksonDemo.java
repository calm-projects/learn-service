package com.learn.java;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * jsonNode 树结构 当我们没有实体类的时候可以关联到树结构
 * jsonParse JsonGenerator 流式api
 *
 * @create: 2022-08-09 22:47
 */
public class JacksonDemo {

    // 可以读取yaml，底层使用的是 org.yaml.snakeyaml.Yaml
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final ObjectMapper mapperJson = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static void main(String[] args) throws IOException {
        String json = "{\"name\": \"tom\", \"age\": 18}";
        JsonNode jsonNode = mapperJson.readValue(json, JsonNode.class);
        String text = jsonNode.get("name").asText();
        System.out.println(text);

        // ObjectNode是JsonNode的子类
        ObjectNode objectNode = mapperJson.createObjectNode();
        objectNode.put("name", "tom");
        objectNode.put("age", 18);

        String res = mapperJson.writeValueAsString(objectNode);
        System.out.println(res);
    }

    /**
     * 测试 时间
     *
     * @return void
     */
    @Test
    public void test_date() throws JsonProcessingException {
        User user = User.builder().name("tom").age(19).birthday01(new Date()).birthday02(LocalDateTime.now()).build();
        ObjectMapper objectMapper01 = new ObjectMapper().findAndRegisterModules();
        ObjectMapper objectMapper02 = new ObjectMapper();
        String s = objectMapper01.writeValueAsString(user);
        String s1 = objectMapper02.writeValueAsString(user);
        System.out.println(s);
        System.out.println(s1);
    }


    /**
     * 测试生成schema
     *
     * @return void
     */
    @Test
    public void test_schema() {
        /*
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(User.class);

        System.out.println(jsonSchema.toPrettyString());
        */
    }
}