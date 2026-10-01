package gg.statikk.gateway.filter;

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
 * IMPORTANTE (v0.1): ms-auth todavía no existe (llega en v0.2), así que este
 * filtro no tiene tokens reales que validar todavía. Está aquí ya cableado
 * para que, en cuanto ms-auth empiece a emitir tokens firmados con el mismo
 * jwt.secret, las rutas protegidas funcionen sin tocar el gateway de nuevo.
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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return reject(exchange, "Falta el header Authorization con un Bearer token");
        }

        String token = authHeader.substring(7);
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return reject(exchange, "Token inválido o expirado: " + ex.getMessage());
        }

        return chain.filter(exchange);
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
