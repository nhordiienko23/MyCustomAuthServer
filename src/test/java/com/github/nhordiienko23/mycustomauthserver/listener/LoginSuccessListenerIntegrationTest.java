package com.github.nhordiienko23.mycustomauthserver.listener;

import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@Transactional
public class LoginSuccessListenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Создаем пользователя для теста логина
        User user = User.builder()
                .username("testloginuser")
                .password(passwordEncoder.encode("secretpassword"))
                .email("login@example.com")
                .registeredAt(new Date())
                // lastLoginAt специально не устанавливаем (null)
                .build();
        userRepository.save(user);
    }

    @Test
    void onSuccessfulLogin_shouldUpdateLastLoginAt() throws Exception {
        // Выполняем логин через стандартную форму Spring Security
        mockMvc.perform(formLogin("/login")
                        .user("testloginuser")
                        .password("secretpassword"))
                .andExpect(status().is3xxRedirection()); // Успешный логин ведет к редиректу

        // Извлекаем пользователя из БД и проверяем, что Listener отработал
        User updatedUser = userRepository.findByUsername("testloginuser").orElseThrow();
        assertNotNull(updatedUser.getLastLoginAt(), "Поле lastLoginAt должно быть обновлено после успешного входа");
    }
}