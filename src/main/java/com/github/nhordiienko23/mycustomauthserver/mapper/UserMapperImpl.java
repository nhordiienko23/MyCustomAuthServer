package com.github.nhordiienko23.mycustomauthserver.mapper;

import com.github.nhordiienko23.mycustomauthserver.dto.UserDto;
import com.github.nhordiienko23.mycustomauthserver.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
