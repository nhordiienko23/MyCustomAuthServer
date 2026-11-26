package com.github.nhordiienko23.mycustomauthserver.config;

import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.service.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationServerConfigTest {

    private final OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer =
            new AuthorizationServerConfig().tokenCustomizer();

    @Test
    @DisplayName("Should add email, name and preferred username to ID token")
    void shouldAddUserClaimsToIdToken() {
        CustomUserDetails userDetails = userDetails("alice", "alice@example.com");
        JwtClaimsSet.Builder claims = newClaimsBuilder();
        JwtEncodingContext context = context("id_token", userDetails, claims);

        tokenCustomizer.customize(context);

        JwtClaimsSet result = claims.build();

        assertAll(
                () -> assertEquals("alice@example.com", result.getClaim("email")),
                () -> assertEquals("alice", result.getClaim("name")),
                () -> assertEquals("alice", result.getClaim("preferred_username")),
                () -> assertEquals("existing", result.getClaim("existing"))
        );
    }

    @Test
    @DisplayName("Should not add user claims to access token")
    void shouldNotAddClaimsToAccessToken() {
        JwtClaimsSet.Builder claims = newClaimsBuilder();
        JwtEncodingContext context = context(
                "access_token",
                userDetails("alice", "alice@example.com"),
                claims
        );

        tokenCustomizer.customize(context);

        JwtClaimsSet result = claims.build();

        assertAll(
                () -> assertEquals(1, result.getClaims().size()),
                () -> assertEquals("existing", result.getClaim("existing")),
                () -> assertNull(result.getClaim("email")),
                () -> assertNull(result.getClaim("name")),
                () -> assertNull(result.getClaim("preferred_username"))
        );
    }

    @Test
    @DisplayName("Should not add claims when principal is not CustomUserDetails")
    void shouldNotAddClaimsForUnsupportedPrincipal() {
        JwtClaimsSet.Builder claims = newClaimsBuilder();
        JwtEncodingContext context = context("id_token", "alice", claims);

        tokenCustomizer.customize(context);

        JwtClaimsSet result = claims.build();

        assertAll(
                () -> assertEquals(1, result.getClaims().size()),
                () -> assertEquals("existing", result.getClaim("existing")),
                () -> assertNull(result.getClaim("email")),
                () -> assertNull(result.getClaim("name")),
                () -> assertNull(result.getClaim("preferred_username"))
        );
    }

    @Test
    @DisplayName("Should preserve blank username and email values in ID token")
    void shouldPreserveBlankUserValues() {
        JwtClaimsSet.Builder claims = newClaimsBuilder();
        JwtEncodingContext context = context(
                "id_token",
                userDetails("", ""),
                claims
        );

        tokenCustomizer.customize(context);

        JwtClaimsSet result = claims.build();

        assertAll(
                () -> assertEquals("", result.getClaim("email")),
                () -> assertEquals("", result.getClaim("name")),
                () -> assertEquals("", result.getClaim("preferred_username"))
        );
    }

    private static JwtClaimsSet.Builder newClaimsBuilder() {
        return JwtClaimsSet.builder()
                .claim("existing", "existing");
    }

    private static CustomUserDetails userDetails(String username, String email) {
        return new CustomUserDetails(User.builder()
                .username(username)
                .email(email)
                .password("password")
                .build());
    }

    private static JwtEncodingContext context(
            String tokenTypeValue,
            Object principal,
            JwtClaimsSet.Builder claims
    ) {
        JwtEncodingContext context = mock(JwtEncodingContext.class);
        OAuth2TokenType tokenType = mock(OAuth2TokenType.class);

        when(tokenType.getValue()).thenReturn(tokenTypeValue);
        when(context.getTokenType()).thenReturn(tokenType);
        when(context.getPrincipal()).thenReturn(
                new UsernamePasswordAuthenticationToken(principal, null)
        );
        when(context.getClaims()).thenReturn(claims);

        return context;
    }
}
