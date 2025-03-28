package com.learn.java.controller;

import com.learn.java.dto.UserDTO;
import com.learn.java.entity.User;
import com.learn.java.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    // : 表示如果没有配置key则直接使用:后面的默认值
    @Value("${uat-url:http://xxx}")
    private String uatUrl;

    @GetMapping("uat")
    public String getUatUrl() {
        return uatUrl;
    }

    @PostMapping("insert")
    public User getUatUrl(@RequestBody UserDTO userDTO) {
        return UserMapper.INSTANCE.userDTOToUser(userDTO);
    }


    @GetMapping("/request")
    public void request(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("msg", "hello world");
        request.setAttribute("code", "200");
        try {
            request.getRequestDispatcher("/user/success").forward(request, response);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    @GetMapping("/success")
    public Map<String, Object> success(
            @RequestAttribute(value = "msg", required = false) String msg,
            @RequestAttribute(value = "code", required = false) Integer code,
            HttpServletRequest request) {
        log.info("execute success .....");
        Map<String, Object> map = new HashMap<>();
        String msg1 = (String) request.getAttribute("msg");
        String code1 = (String) request.getAttribute("code");
        map.put("msg", msg);
        map.put("msg1", msg1);
        map.put("code", code);
        map.put("code1", code1);
        return map;
    }
}
