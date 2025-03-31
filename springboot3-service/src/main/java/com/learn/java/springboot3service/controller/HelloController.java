package com.learn.java.springboot3service.controller;

import com.learn.java.springboot3service.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hello")
@Slf4j
public class HelloController {
    @GetMapping("/{name}")
    public String hello(@PathVariable(name = "name", required = false) String name) {
        String res = "hello " + name;
        log.info("HelloController-hello-get: {}", res);
        return res;
    }

    @PostMapping("/")
    public UserDTO hello(@RequestBody UserDTO userDTO) {
        log.info("HelloController-hello-post: {}", userDTO);
        return userDTO;
    }
}
