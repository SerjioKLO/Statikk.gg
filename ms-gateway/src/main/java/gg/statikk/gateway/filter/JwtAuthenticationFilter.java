package gg.statikk.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Valida el JWT en el header Authorization para todas las rutas que no estén
 * en la lista blanca (jwt.public-paths).
 *
 * Además (desde v0.3), una vez validado el token, extrae sus claims y las
 * reenvía como headers X-User-Id / X-Username / X-User-Role hacia el
 * microservicio downstream (ej. ms-player), para que este no tenga que
 * volver a parsear el JWT y simplemente confíe en el gateway.
 *
 * En rutas públicas, esos mismos headers se eliminan si vienen del cliente,
 * para que nadie pueda hacerse pasar por otro usuario llamando directo con
 * "X-User-Id: 1" sin haber pasado por el login.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final SecretKey signingKey;
    private final List<String> publicPaths;

    public JwtAuthenticationFilter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.public-paths}") String publicPathsCsv) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.publicPaths = Arrays.asList(publicPathsCsv.split(","));
    }

    private static final List<String> IDENTITY_HEADERS = List.of("X-User-Id", "X-Username", "X-User-Role");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublic(path)) {
            // Nadie puede colarse como "autenticado" en una ruta pública suplantando el header.
            ServerHttpRequest sanitized = stripIdentityHeaders(request);
            return chain.filter(exchange.mutate().request(sanitized).build());
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return reject(exchange, "Falta el header Authorization con un Bearer token");
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(signingKey).build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return reject(exchange, "Token inválido o expirado: " + ex.getMessage());
        }

        ServerHttpRequest mutatedRequest = stripIdentityHeaders(request).mutate()
                .header("X-User-Id", String.valueOf(claims.get("userId")))
                .header("X-Username", String.valueOf(claims.get("username")))
                .header("X-User-Role", String.valueOf(claims.get("role")))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private ServerHttpRequest stripIdentityHeaders(ServerHttpRequest request) {
        return request.mutate()
                .headers(httpHeaders -> IDENTITY_HEADERS.forEach(httpHeaders::remove))
                .build();
    }

    private boolean isPublic(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> reject(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Auth-Error", reason);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // se ejecuta antes que los filtros de ruteo
    }
}
