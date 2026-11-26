package com.github.nhordiienko23.mycustomauthserver.listener;

import com.github.nhordiienko23.mycustomauthserver.AbstractPostgreSqlIntegrationTest;
import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Login success listener integration")
class LoginSuccessListenerIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder()
                .username("testloginuser")
                .password(passwordEncoder.encode("secretpassword"))
                .email("login@example.com")
                .registeredAt(new Date())
                .build());
    }

    @Test
    @DisplayName("Should update last login time after successful authentication")
    void onSuccessfulLogin_shouldUpdateLastLoginAt() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("testloginuser")
                        .password("secretpassword"))
                .andExpect(status().is3xxRedirection());

        User updatedUser = userRepository.findByUsername("testloginuser").orElseThrow();
        assertNotNull(updatedUser.getLastLoginAt());
    }

    @Test
    @DisplayName("Should not update last login time after failed authentication")
    void onFailedLogin_shouldLeaveLastLoginAtUnchanged() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("testloginuser")
                        .password("wrong-password"))
                .andExpect(status().is3xxRedirection());

        User unchangedUser = userRepository.findByUsername("testloginuser").orElseThrow();
        assertNull(unchangedUser.getLastLoginAt());
    }
}
