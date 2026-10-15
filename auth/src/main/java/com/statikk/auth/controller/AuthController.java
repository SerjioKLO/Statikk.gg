package com.statikk.auth.controller;

import com.statikk.auth.dto.*;
import com.statikk.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * RF-01: Registrar Usuarios
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * RF-02: Ingresar Usuarios (Login)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate JWT Token
     */
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestParam("token") String token) {
        TokenValidationResponse response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Get User Profile by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable("id") Long id) {
        UserDto userDto = authService.getUserProfile(id);
        return ResponseEntity.ok(userDto);
    }
}
