package com.github.nhordiienko23.mycustomauthserver.mapper;

import com.github.nhordiienko23.mycustomauthserver.dto.UserDto;
import com.github.nhordiienko23.mycustomauthserver.entity.User;

public interface UserMapper {
    UserDto toUserDto(User user);
}
