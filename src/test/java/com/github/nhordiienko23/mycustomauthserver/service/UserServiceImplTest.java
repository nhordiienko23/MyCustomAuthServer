package com.github.nhordiienko23.mycustomauthserver.service;

import com.github.nhordiienko23.mycustomauthserver.dto.RegisterRequest;
import com.github.nhordiienko23.mycustomauthserver.dto.UserDto;
import com.github.nhordiienko23.mycustomauthserver.entity.User;
import com.github.nhordiienko23.mycustomauthserver.mapper.UserMapper;
import com.github.nhordiienko23.mycustomauthserver.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User registration service")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    @DisplayName("Should encode the password, save the user, and return the mapped DTO")
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request =
                new RegisterRequest("alice", "plain-password", "alice@example.com");
        UserDto expectedDto = UserDto.builder()
                .username("alice")
                .email("alice@example.com")
                .build();

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.<User>getArgument(0));
        when(userMapper.toUserDto(any(User.class))).thenReturn(expectedDto);

        Date beforeRegistration = new Date();
        UserDto result = service.register(request);
        Date afterRegistration = new Date();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).existsByUsername("alice");
        verify(passwordEncoder).encode("plain-password");
        verify(userRepository).save(userCaptor.capture());
        verify(userMapper).toUserDto(userCaptor.getValue());

        User savedUser = userCaptor.getValue();
        assertAll(
                () -> assertSame(expectedDto, result),
                () -> assertEquals("alice", savedUser.getUsername()),
                () -> assertEquals("alice@example.com", savedUser.getEmail()),
                () -> assertEquals("encoded-password", savedUser.getPassword()),
                () -> assertNotNull(savedUser.getRegisteredAt()),
                () -> assertFalse(savedUser.getRegisteredAt().before(beforeRegistration)),
                () -> assertFalse(savedUser.getRegisteredAt().after(afterRegistration))
        );
        verifyNoMoreInteractions(userRepository, passwordEncoder, userMapper);
    }

    @Test
    @DisplayName("Should reject a username that is already taken without saving or encoding")
    void shouldRejectDuplicateUsername() {
        RegisterRequest request =
                new RegisterRequest("alice", "plain-password", "alice@example.com");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.register(request)
        );

        assertEquals("Username is already taken!", exception.getMessage());
        verify(userRepository).existsByUsername("alice");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder, userMapper);
    }

    @Test
    @DisplayName("Should propagate username lookup failures without continuing registration")
    void shouldPropagateUsernameLookupFailure() {
        RegisterRequest request =
                new RegisterRequest("alice", "plain-password", "alice@example.com");
        RuntimeException failure = new RuntimeException("lookup failed");
        when(userRepository.existsByUsername("alice")).thenThrow(failure);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.register(request)
        );

        assertSame(failure, thrown);
        verify(userRepository).existsByUsername("alice");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder, userMapper);
    }

    @Test
    @DisplayName("Should propagate password encoding failures without saving the user")
    void shouldPropagatePasswordEncodingFailure() {
        RegisterRequest request =
                new RegisterRequest("alice", "plain-password", "alice@example.com");
        RuntimeException failure = new RuntimeException("encoding failed");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenThrow(failure);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.register(request)
        );

        assertSame(failure, thrown);
        verify(userRepository).existsByUsername("alice");
        verifyNoMoreInteractions(userRepository);
        verify(passwordEncoder).encode("plain-password");
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should propagate repository save failures without mapping the user")
    void shouldPropagateSaveFailure() {
        RegisterRequest request =
                new RegisterRequest("alice", "plain-password", "alice@example.com");
        RuntimeException failure = new RuntimeException("save failed");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenThrow(failure);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.register(request)
        );

        assertSame(failure, thrown);
        verify(userRepository).existsByUsername("alice");
        verify(userRepository).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
        verify(passwordEncoder).encode("plain-password");
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should propagate mapping failures after saving the user")
    void shouldPropagateMappingFailure() {
        RegisterRequest request =
                new RegisterRequest("alice", "plain-password", "alice@example.com");
        RuntimeException failure = new RuntimeException("mapping failed");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.<User>getArgument(0));
        when(userMapper.toUserDto(any(User.class))).thenThrow(failure);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> service.register(request)
        );

        assertSame(failure, thrown);
        verify(userRepository).existsByUsername("alice");
        verify(userRepository).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
        verify(passwordEncoder).encode("plain-password");
        verifyNoMoreInteractions(passwordEncoder);
        verify(userMapper).toUserDto(any(User.class));
        verifyNoMoreInteractions(userMapper);
    }
}