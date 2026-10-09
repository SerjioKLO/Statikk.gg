package gg.statikk.matches.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * "ms-player" es el nombre lógico registrado en Eureka (spring.application.name
 * de ese servicio) — Feign + el load balancer resuelven la IP/puerto real.
 */
@FeignClient(name = "ms-player")
public interface PlayerClient {

    @GetMapping("/api/players/{id}")
    PlayerProfileClientResponse getProfile(@PathVariable("id") Long id);
}
