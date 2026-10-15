package com.statikk.auth.service;

import com.statikk.auth.dto.AuthResponse;
import com.statikk.auth.dto.LoginRequest;
import com.statikk.auth.dto.RegisterRequest;
import com.statikk.auth.dto.TokenValidationResponse;
import com.statikk.auth.entity.User;
import com.statikk.auth.exception.InvalidCredentialsException;
import com.statikk.auth.exception.UserAlreadyExistsException;
import com.statikk.auth.repository.UserRepository;
import com.statikk.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("faker")
                .email("faker@t1.gg")
                .password("encoded_secret_pwd")
                .role("ROLE_USER")
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("RF-01: Should register user successfully when data is valid")
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .username("faker")
                .email("faker@t1.gg")
                .password("plain_pwd")
                .build();

        when(userRepository.existsByEmail("faker@t1.gg")).thenReturn(false);
        when(userRepository.existsByUsername("faker")).thenReturn(false);
        when(passwordEncoder.encode("plain_pwd")).thenReturn("encoded_secret_pwd");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtService.generateToken(sampleUser)).thenReturn("mocked_jwt_token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked_jwt_token");
        assertThat(response.getEmail()).isEqualTo("faker@t1.gg");
        assertThat(response.getMessage()).isEqualTo("Cuenta creada exitosamente");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("RF-01 Exception: Should throw UserAlreadyExistsException when email already registered")
    void shouldThrowExceptionWhenEmailExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("faker2")
                .email("faker@t1.gg")
                .password("plain_pwd")
                .build();

        when(userRepository.existsByEmail("faker@t1.gg")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email is already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("RF-02: Should login user successfully with valid credentials")
    void shouldLoginUserSuccessfully() {
        LoginRequest request = LoginRequest.builder()
                .email("faker@t1.gg")
                .password("plain_pwd")
                .build();

        when(userRepository.findByEmail("faker@t1.gg")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("plain_pwd", "encoded_secret_pwd")).thenReturn(true);
        when(jwtService.generateToken(sampleUser)).thenReturn("mocked_jwt_token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked_jwt_token");
        assertThat(response.getMessage()).isEqualTo("Acceso concedido");
    }

    @Test
    @DisplayName("RF-02 Exception: Should throw InvalidCredentialsException when password does not match")
    void shouldThrowExceptionWhenPasswordInvalid() {
        LoginRequest request = LoginRequest.builder()
                .email("faker@t1.gg")
                .password("wrong_pwd")
                .build();

        when(userRepository.findByEmail("faker@t1.gg")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrong_pwd", "encoded_secret_pwd")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Contraseña o usuario inválidos");
    }

    @Test
    @DisplayName("Should validate active JWT token")
    void shouldValidateToken() {
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUserId("valid-token")).thenReturn(1L);
        when(jwtService.extractEmail("valid-token")).thenReturn("faker@t1.gg");
        when(jwtService.extractRole("valid-token")).thenReturn("ROLE_USER");

        TokenValidationResponse response = authService.validateToken("Bearer valid-token");

        assertThat(response.isValid()).isTrue();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("faker@t1.gg");
    }
}
