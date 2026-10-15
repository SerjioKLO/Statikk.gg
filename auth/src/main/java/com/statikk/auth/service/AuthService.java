package com.statikk.auth.service;

import com.statikk.auth.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    TokenValidationResponse validateToken(String token);
    UserDto getUserProfile(Long userId);
}
