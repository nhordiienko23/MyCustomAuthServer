package com.github.nhordiienko23.mycustomauthserver.service;

import com.github.nhordiienko23.mycustomauthserver.dto.RegisterRequest;
import com.github.nhordiienko23.mycustomauthserver.dto.UserDto;
import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.mapper.UserMapper;
import com.github.nhordiienko23.mycustomauthserver.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto register(RegisterRequest registerRequest) {
        if(userRepository.existsByUsername(registerRequest.username())){
            throw new IllegalArgumentException("Username is already taken!");
        }
        User user = User.builder()
                .username(registerRequest.username())
                .password(passwordEncoder.encode(registerRequest.password()))
                .email(registerRequest.email())
                .registeredAt(new Date())
                .build();
        User savedUser = userRepository.save(user);
        return userMapper.toUserDto(savedUser);
    }

}
