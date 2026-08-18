package com.github.nhordiienko23.mycustomauthserver.controller;

import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
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
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("success"));

        assertTrue(userRepository.existsByUsername("newuser"));
    }

    @Test
    void registerUser_whenUsernameExists_shouldReturnError() throws Exception {
        User existingUser = User.builder()
                .username("existinguser")
                .password("encodedpass")
                .email("existing@example.com")
                .registeredAt(new Date())
                .build();
        userRepository.save(existingUser);

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

    @Test
    void home_whenAuthenticated_shouldReturnHomeViewWithUsername() throws Exception {
        // Создаем и сохраняем юзера в тестовую базу H2
        User user = User.builder()
                .username("homeuser")
                .password(passwordEncoder.encode("password"))
                .email("home@example.com")
                .registeredAt(new Date())
                .build();
        userRepository.save(user);

        // Выполняем GET-запрос с аутентификацией (springSecurity() постпроцессор)
        mockMvc.perform(get("/")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("homeuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("username", "homeuser"));
    }
}