package gg.statikk.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * ms-registry: service discovery de Statikk.gg.
 * Todos los demás microservicios (ms-auth, ms-player, ms-matches, ms-stats,
 * ms-excel-parser, ms-notification) se registran aquí al arrancar, y
 * ms-gateway lo consulta para resolver a qué instancia enviar cada petición.
 */
@SpringBootApplication
@EnableEurekaServer
public class RegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistryApplication.class, args);
    }
}
