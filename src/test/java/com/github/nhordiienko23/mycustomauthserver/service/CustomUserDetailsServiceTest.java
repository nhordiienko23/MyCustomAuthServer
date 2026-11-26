package com.github.nhordiienko23.mycustomauthserver.service;

import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Custom user details service")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    @DisplayName("Should return user details for an existing username")
    void shouldReturnUserDetailsForExistingUsername() {
        User user = User.builder()
                .username("alice")
                .email("alice@example.com")
                .password("encoded-password")
                .build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("alice");

        assertAll(
                () -> assertInstanceOf(CustomUserDetails.class, details),
                () -> assertEquals("alice", details.getUsername()),
                () -> assertEquals("encoded-password", details.getPassword()),
                () -> assertEquals("alice@example.com",
                        ((CustomUserDetails) details).getEmail())
        );
        verify(userRepository).findByUsername("alice");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when the username does not exist")
    void shouldThrowWhenUsernameDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing")
        );

        assertEquals("User not found with username: missing", exception.getMessage());
        verify(userRepository).findByUsername("missing");
        verifyNoMoreInteractions(userRepository);
    }

    @ParameterizedTest(name = "username={0}")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\talice\t", "alice+tag@example.com"})
    @DisplayName("Should pass unusual usernames unchanged and report when they are not found")
    void shouldHandleUnusualUsernames(String username) {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(username)
        );

        assertEquals("User not found with username: " + username, exception.getMessage());
        verify(userRepository).findByUsername(username);
        verifyNoMoreInteractions(userRepository);
    }
}