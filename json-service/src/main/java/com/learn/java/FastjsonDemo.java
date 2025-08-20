package com.learn.java;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 * 描述信息
 *
 * @create: 2022-08-09 23:08
 */
public class FastjsonDemo {
    public static void main(String[] args) {
        // 其实就是一个map
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "tom");
        jsonObject.put("age", 18);

        String res = jsonObject.toJSONString();
        System.out.println(res);

        String json = "{\"name\":\"tom\",\"age\":18}";
        JSONObject jsonObject1 = JSON.parseObject(json);
        String res1 = JSON.toJSONString(jsonObject1);
        System.out.println(res1);
    }
}