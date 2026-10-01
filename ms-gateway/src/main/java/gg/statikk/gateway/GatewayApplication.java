package gg.statikk.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ms-gateway: punto de entrada único de Statikk.gg.
 * Enruta las peticiones hacia los microservicios registrados en ms-registry,
 * aplica rate limiting (Redis) y validación de JWT en las rutas protegidas.
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
