package com.github.nhordiienko23.mycustomauthserver.config;

import com.github.nhordiienko23.mycustomauthserver.AbstractPostgreSqlIntegrationTest;
import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.service.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@Transactional
@DisplayName("OAuth2 token customizer integration")
class TokenCustomizerIntegrationTest extends AbstractPostgreSqlIntegrationTest {

    @Autowired
    private OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer;

    @Test
    @DisplayName("Should add user claims to ID tokens")
    void tokenCustomizer_shouldAddEmailClaimToIdToken() {
        User user = User.builder()
                .username("jwtuser")
                .password("pass")
                .email("jwtuser@example.com")
                .registeredAt(new Date())
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        JwtEncodingContext context = mock(JwtEncodingContext.class);
        OAuth2TokenType tokenType = mock(OAuth2TokenType.class);
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder().claim("existing", "value");
        when(tokenType.getValue()).thenReturn("id_token");
        when(context.getTokenType()).thenReturn(tokenType);
        when(context.getPrincipal()).thenReturn(principal);
        when(context.getClaims()).thenReturn(claims);

        tokenCustomizer.customize(context);

        JwtClaimsSet result = claims.build();
        assertAll(
                () -> assertEquals("value", result.getClaim("existing")),
                () -> assertEquals("jwtuser@example.com", result.getClaim("email")),
                () -> assertEquals("jwtuser", result.getClaim("name")),
                () -> assertEquals("jwtuser", result.getClaim("preferred_username"))
        );
    }

    @Test
    @DisplayName("Should not add user claims to access tokens")
    void tokenCustomizer_shouldNotAddClaimsToAccessToken() {
        JwtEncodingContext context = mock(JwtEncodingContext.class);
        OAuth2TokenType tokenType = mock(OAuth2TokenType.class);
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder().claim("existing", "value");
        when(tokenType.getValue()).thenReturn("access_token");
        when(context.getTokenType()).thenReturn(tokenType);
        when(context.getClaims()).thenReturn(claims);

        tokenCustomizer.customize(context);

        JwtClaimsSet result = claims.build();
        assertAll(
                () -> assertEquals("value", result.getClaim("existing")),
                () -> assertNull(result.getClaim("email")),
                () -> assertNull(result.getClaim("name")),
                () -> assertNull(result.getClaim("preferred_username"))
        );
    }
}
