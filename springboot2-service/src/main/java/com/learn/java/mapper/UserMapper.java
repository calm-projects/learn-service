package com.learn.java.mapper;

import com.learn.java.dto.UserDTO;
import com.learn.java.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDTO userToUserDTO(User car);

    User userDTOToUser(UserDTO userDTO);
}