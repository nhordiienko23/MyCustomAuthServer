package com.github.nhordiienko23.mycustomauthserver.config;

import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Login success listener")
class LoginSuccessListenerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Test
    @DisplayName("Should update and save the matching user's last login time")
    void shouldUpdateAndSaveLastLoginTime() {
        User user = User.builder()
                .username("alice")
                .lastLoginAt(new Date(0))
                .build();

        UserDetails principal = userDetails("alice");

        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(user));

        Date beforeEvent = new Date();

        new LoginSuccessListener(userRepository)
                .onApplicationEvent(new AuthenticationSuccessEvent(authentication));

        Date afterEvent = new Date();
        Date lastLoginAt = user.getLastLoginAt();

        assertAll(
                () -> assertNotNull(lastLoginAt),
                () -> assertFalse(lastLoginAt.before(beforeEvent)),
                () -> assertFalse(lastLoginAt.after(afterEvent)),
                () -> assertNotSame(new Date(0), lastLoginAt)
        );

        verify(userRepository).findByUsername("alice");
        verify(userRepository).save(same(user));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should not save when the principal's user does not exist")
    void shouldNotSaveWhenUserDoesNotExist() {
        UserDetails principal = userDetails("missing");

        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        new LoginSuccessListener(userRepository)
                .onApplicationEvent(new AuthenticationSuccessEvent(authentication));

        verify(userRepository).findByUsername("missing");
        verifyNoMoreInteractions(userRepository);
    }

    @ParameterizedTest(name = "principal={0}")
    @NullSource
    @ValueSource(strings = {"anonymous", ""})
    @DisplayName("Should ignore principals that are not UserDetails")
    void shouldIgnoreNonUserDetailsPrincipals(Object principal) {
        when(authentication.getPrincipal()).thenReturn(principal);

        new LoginSuccessListener(userRepository)
                .onApplicationEvent(new AuthenticationSuccessEvent(authentication));

        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest(name = "username={0}")
    @NullSource
    @ValueSource(strings = {"", " ", "\talice\t", "alice+tag@example.com"})
    @DisplayName("Should pass unusual usernames unchanged to the repository")
    void shouldPassUnusualUsernamesUnchanged(String username) {
        UserDetails principal = userDetails(username);

        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        new LoginSuccessListener(userRepository)
                .onApplicationEvent(new AuthenticationSuccessEvent(authentication));

        verify(userRepository).findByUsername(username);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should propagate repository lookup failures")
    void shouldPropagateLookupFailure() {
        RuntimeException failure = new RuntimeException("lookup failed");
        UserDetails principal = userDetails("alice");

        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByUsername("alice")).thenThrow(failure);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> new LoginSuccessListener(userRepository)
                        .onApplicationEvent(new AuthenticationSuccessEvent(authentication))
        );

        assertSame(failure, thrown);

        verify(userRepository).findByUsername("alice");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should propagate save failures")
    void shouldPropagateSaveFailure() {
        User user = User.builder()
                .username("alice")
                .build();

        RuntimeException failure = new RuntimeException("save failed");
        UserDetails principal = userDetails("alice");

        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenThrow(failure);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> new LoginSuccessListener(userRepository)
                        .onApplicationEvent(new AuthenticationSuccessEvent(authentication))
        );

        assertSame(failure, thrown);
        assertNotNull(user.getLastLoginAt());

        verify(userRepository).findByUsername("alice");
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository);
    }

    private UserDetails userDetails(String username) {
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn(username);
        return principal;
    }
}
