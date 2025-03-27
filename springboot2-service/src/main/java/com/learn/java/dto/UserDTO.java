package com.learn.java.dto;

import com.learn.java.entity.User;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

@Data(staticConstructor = "of")
@Accessors(chain = true)
public class UserDTO {
    public UserDTO() {
    }

    private String name;

    public User toEntity() {
        User entity = new User();
        BeanUtils.copyProperties(this, entity);
        return entity;
    }
}
