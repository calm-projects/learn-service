package com.learn.java.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data(staticConstructor = "of")
@Accessors(chain = true)
public class User {
    public User() {
    }
    private String name;
    private Animal animal;
}