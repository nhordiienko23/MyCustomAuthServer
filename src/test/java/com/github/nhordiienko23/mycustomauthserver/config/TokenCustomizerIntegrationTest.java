package com.github.nhordiienko23.mycustomauthserver.config;

import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.service.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
public class TokenCustomizerIntegrationTest {

    @Autowired
    private OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer;

    @Test
    void tokenCustomizer_shouldAddEmailClaimToIdToken() {
        // Подготавливаем пользователя
        User user = User.builder()
                .username("jwtuser")
                .password("pass")
                .email("jwtuser@example.com")
                .registeredAt(new Date())
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // Создаем мок контекста
        JwtEncodingContext context = mock(JwtEncodingContext.class);
        OAuth2TokenType tokenType = mock(OAuth2TokenType.class);

        // Настраиваем поведение моков, используя безопасное строковое сравнение
        when(tokenType.getValue()).thenReturn("id_token");
        when(context.getTokenType()).thenReturn(tokenType);
        when(context.getPrincipal()).thenReturn(principal);
    }
}