package com.github.nhordiienko23.mycustomauthserver.service;

import com.github.nhordiienko23.mycustomauthserver.dto.RegisterRequest;
import com.github.nhordiienko23.mycustomauthserver.dto.UserDto;

public interface UserService {
    UserDto register(RegisterRequest registerRequest);
}
