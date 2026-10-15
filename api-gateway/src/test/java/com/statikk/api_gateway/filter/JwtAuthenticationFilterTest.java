package com.statikk.api_gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtValidator jwtValidator;

    @Mock
    private GatewayFilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtValidator);
    }

    @Test
    @DisplayName("Should pass open endpoints without JWT authentication")
    void shouldAllowOpenEndpoints() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, times(1)).filter(exchange);
        verifyNoInteractions(jwtValidator);
    }

    @Test
    @DisplayName("Should block protected endpoint when Authorization header is missing")
    void shouldRejectWhenMissingAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/players/1").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(filterChain);
    }

    @Test
    @DisplayName("Should authenticate and pass downstream headers when token is valid")
    void shouldAuthenticateValidTokenAndSetHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/players/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtValidator.validateToken("valid-token")).thenReturn(true);
        when(jwtValidator.extractUserId("valid-token")).thenReturn("1");
        when(jwtValidator.extractEmail("valid-token")).thenReturn("user@statikk.gg");
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, times(1)).filter(any());
    }
}
