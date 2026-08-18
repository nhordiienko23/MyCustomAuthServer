package com.github.nhordiienko23.mycustomauthserver.controller;

import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@Transactional // Откатывает изменения в БД после каждого теста
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void showRegistrationForm_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void registerUser_shouldSaveUserAndReturnSuccess() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "newuser")
                        .param("password", "password123")
                        .param("email", "newuser@example.com")
                        .with(csrf())) // Обязательно для POST-запросов при включенном Spring Security
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("success"));

        assertTrue(userRepository.existsByUsername("newuser"));
    }

    @Test
    void registerUser_whenUsernameExists_shouldReturnError() throws Exception {
        // Подготавливаем существующего пользователя
        User existingUser = User.builder()
                .username("existinguser")
                .password("encodedpass")
                .email("existing@example.com")
                .registeredAt(new Date())
                .build();
        userRepository.save(existingUser);

        // Пытаемся зарегистрировать с тем же username
        mockMvc.perform(post("/register")
                        .param("username", "existinguser")
                        .param("password", "newpass")
                        .param("email", "another@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Username is already taken!"));
    }
}