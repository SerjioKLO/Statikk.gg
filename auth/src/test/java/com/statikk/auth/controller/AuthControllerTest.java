package com.statikk.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.statikk.auth.config.JwtAuthenticationFilter;
import com.statikk.auth.dto.AuthResponse;
import com.statikk.auth.dto.LoginRequest;
import com.statikk.auth.dto.RegisterRequest;
import com.statikk.auth.service.AuthService;
import com.statikk.auth.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/auth/register - RF-01: Should register user and return 201 CREATED")
    void shouldRegisterUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("showmaker")
                .email("showmaker@dk.gg")
                .password("password123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt_token_sample")
                .userId(1L)
                .username("showmaker")
                .email("showmaker@dk.gg")
                .message("Cuenta creada exitosamente")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt_token_sample"))
                .andExpect(jsonPath("$.email").value("showmaker@dk.gg"))
                .andExpect(jsonPath("$.message").value("Cuenta creada exitosamente"));
    }

    @Test
    @DisplayName("POST /api/auth/login - RF-02: Should authenticate user and return 200 OK")
    void shouldLoginUser() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("showmaker@dk.gg")
                .password("password123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt_token_sample")
                .userId(1L)
                .username("showmaker")
                .email("showmaker@dk.gg")
                .message("Acceso concedido")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token_sample"))
                .andExpect(jsonPath("$.message").value("Acceso concedido"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Validation Failure: Should return 400 BAD_REQUEST on invalid email")
    void shouldRejectInvalidEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("sm")
                .email("not-an-email")
                .password("123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }
}
