package com.github.nhordiienko23.mycustomauthserver.service;

import com.github.nhordiienko23.mycustomauthserver.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    @DisplayName("Should delegate username, email and password to user")
    void shouldDelegateUserProperties() {
        User user = User.builder()
                .username("alice")
                .email("alice@example.com")
                .password("$2a$encoded-password")
                .build();

        CustomUserDetails details = new CustomUserDetails(user);

        assertAll(
                () -> assertEquals("alice", details.getUsername()),
                () -> assertEquals("alice@example.com", details.getEmail()),
                () -> assertEquals("$2a$encoded-password", details.getPassword())
        );
    }

    @Test
    @DisplayName("Should return exactly the ROLE_USER authority")
    void shouldReturnExactlyUserRole() {
        CustomUserDetails details = new CustomUserDetails(User.builder().build());

        List<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertEquals(List.of("ROLE_USER"), authorities);
    }


    @Test
    @DisplayName("Should report all account status flags as true")
    void shouldReportAllAccountStatusFlagsAsTrue() {
        CustomUserDetails details = new CustomUserDetails(User.builder().build());

        assertAll(
                () -> assertTrue(details.isAccountNonExpired()),
                () -> assertTrue(details.isAccountNonLocked()),
                () -> assertTrue(details.isCredentialsNonExpired()),
                () -> assertTrue(details.isEnabled())
        );
    }

    @Test
    @DisplayName("Should preserve null and empty user properties")
    void shouldPreserveNullAndEmptyUserProperties() {
        User user = User.builder()
                .username("")
                .email(null)
                .password("")
                .build();

        CustomUserDetails details = new CustomUserDetails(user);

        assertAll(
                () -> assertEquals("", details.getUsername()),
                () -> assertNull(details.getEmail()),
                () -> assertEquals("", details.getPassword())
        );
    }

    @Test
    @DisplayName("Should reject a null user")
    void shouldRejectNullUser() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new CustomUserDetails(null)
        );

        assertEquals("User must not be null", exception.getMessage());
    }
}
