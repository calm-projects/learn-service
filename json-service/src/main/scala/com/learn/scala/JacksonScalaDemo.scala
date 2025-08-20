package com.learn.scala

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.{DeserializationFeature, JsonNode, ObjectMapper}
import com.fasterxml.jackson.dataformat.yaml.{YAMLFactory, YAMLGenerator, YAMLMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.learn.java.User

object JacksonScalaDemo {
  def main(args: Array[String]): Unit = {
    val mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    val json = "{\"name\": \"tom\", \"age\": 18}"
    val jsonNode = mapper.readValue(json, classOf[JsonNode])
    val name = jsonNode.get("name").asText
    println(s"name: $name")

    // ObjectNode是JsonNode的子类
    val objectNode = mapper.createObjectNode
    objectNode.put("name", "tom")
    objectNode.put("age", 18)

    val res = mapper.writeValueAsString(objectNode)
    println(s"res: ${res}")
  }

  /**
   * 在scala 中如何使用json
   *
   * */
  def test_yaml(): Unit = {
    // 直接创建 YAMLMapper ObjectMapper的子类
    val mapper = new YAMLMapper();
    val user = mapper.readValue("yamlSource", classOf[User])

    // 通过builder 的方式创建 YAMLMapper
    val mapper01 = YAMLMapper.builder()
      .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
      .build();

    // YAMLParser 流式解析器
    val factory = new YAMLFactory();
    val parser = factory.createParser("yamlString")
    while (parser.nextToken != null) {
    }

    /*val loaderOptions = new LoaderOptions()
    loaderOptions.setCodePointLimit(10 * 1024 * 1024) // 10 MB
    val yamlFactory = YAMLFactory.builder.loaderOptions(loaderOptions).build
    val mapper = new YAMLMapper(yamlFactory)*/
  }

  /**
   * 在scala 中如何使用json
   *
   * */
  def test_json(): Unit = {
    // ObjectMapper的子类，仅仅支持json格式
    val jsonMapper: JsonMapper = JsonMapper.builder()
      // 要在Jackson中使用Scala模块，需将 DefaultScalaModule 注册到ObjectMapper实例
      .addModule(DefaultScalaModule)
      .disable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
      .build()

    val objectMapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)
    val myMap = objectMapper.readValue("json字符串", new TypeReference[Map[String, (Int, Int)]] {})

  }
}
