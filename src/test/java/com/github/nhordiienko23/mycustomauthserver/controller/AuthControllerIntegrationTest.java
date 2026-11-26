package com.github.nhordiienko23.mycustomauthserver.controller;

import com.github.nhordiienko23.mycustomauthserver.AbstractPostgreSqlIntegrationTest;
import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Authentication controller integration")
class AuthControllerIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should display the registration form")
    void showRegistrationForm_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @DisplayName("Should register a user with an encoded password")
    void registerUser_shouldSaveUserAndReturnSuccess() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "newuser")
                        .param("password", "password123")
                        .param("email", "newuser@example.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("success"));

        User registeredUser = userRepository.findByUsername("newuser").orElseThrow();
        assertAll(
                () -> assertNotNull(registeredUser.getId()),
                () -> assertEquals("newuser@example.com", registeredUser.getEmail()),
                () -> assertTrue(passwordEncoder.matches("password123", registeredUser.getPassword())),
                () -> assertNotNull(registeredUser.getRegisteredAt())
        );
    }

    @Test
    @DisplayName("Should show an error when the username is already taken")
    void registerUser_whenUsernameExists_shouldReturnError() throws Exception {
        userRepository.save(User.builder()
                .username("existinguser")
                .password("encodedpass")
                .email("existing@example.com")
                .registeredAt(new Date())
                .build());

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
    @DisplayName("Should reject registration requests without a CSRF token")
    void registerUser_withoutCsrfToken_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "csrfuser")
                        .param("password", "password123")
                        .param("email", "csrf@example.com"))
                .andExpect(status().isForbidden());

        assertFalse(userRepository.existsByUsername("csrfuser"));
    }

    @Test
    @DisplayName("Should redirect unauthenticated users from the home page to login")
    void home_whenUnauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Should show the home page and username for an authenticated user")
    void home_whenAuthenticated_shouldReturnHomeViewWithUsername() throws Exception {
        userRepository.save(User.builder()
                .username("homeuser")
                .password(passwordEncoder.encode("password"))
                .email("home@example.com")
                .registeredAt(new Date())
                .build());

        mockMvc.perform(get("/")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("homeuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("username", "homeuser"));
    }
}
