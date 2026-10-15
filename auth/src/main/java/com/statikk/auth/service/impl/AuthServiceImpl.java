package com.statikk.auth.service.impl;

import com.statikk.auth.dto.*;
import com.statikk.auth.entity.User;
import com.statikk.auth.exception.InvalidCredentialsException;
import com.statikk.auth.exception.ResourceNotFoundException;
import com.statikk.auth.exception.UserAlreadyExistsException;
import com.statikk.auth.repository.UserRepository;
import com.statikk.auth.service.AuthService;
import com.statikk.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} is already registered", request.getEmail());
            throw new UserAlreadyExistsException("Email is already registered: " + request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username {} is already taken", request.getUsername());
            throw new UserAlreadyExistsException("Username is already taken: " + request.getUsername());
        }

        // Hashing password with BCrypt (RNF-02 Seguridad)
        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .message("Cuenta creada exitosamente")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Contraseña o usuario inválidos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email {}", request.getEmail());
            throw new InvalidCredentialsException("Contraseña o usuario inválidos");
        }

        String token = jwtService.generateToken(user);
        log.info("Login successful for user ID: {}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .message("Acceso concedido")
                .build();
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        if (token == null || token.isBlank()) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token cannot be blank")
                    .build();
        }

        String cleanToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();

        if (!jwtService.isTokenValid(cleanToken)) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token is invalid or expired")
                    .build();
        }

        Long userId = jwtService.extractUserId(cleanToken);
        String email = jwtService.extractEmail(cleanToken);
        String role = jwtService.extractRole(cleanToken);

        return TokenValidationResponse.builder()
                .valid(true)
                .userId(userId)
                .email(email)
                .role(role)
                .message("Token is valid")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
