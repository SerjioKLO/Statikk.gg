package gg.statikk.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import gg.statikk.notification.websocket.HeaderUserHandshakeHandler;
import gg.statikk.notification.websocket.RequireUserHeaderInterceptor;
import gg.statikk.notification.websocket.StompSecurityInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * Cliente (vía gateway):  ws://host:8080/ws/notifications?access_token=<JWT>
 *   -> CONNECT -> SUBSCRIBE /user/queue/notifications -> recibe NotificationResponse en JSON.
 *
 * Usa el broker SIMPLE en memoria: sirve para UNA instancia de este servicio. Para escalar a varias
 * instancias habría que cambiar a un broker externo (enableStompBrokerRelay con el plugin STOMP de RabbitMQ).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String ENDPOINT = "/ws/notifications";
    public static final String USER_QUEUE = "/queue/notifications";

    private final ObjectMapper objectMapper;
    private final String[] allowedOrigins;

    public WebSocketConfig(ObjectMapper objectMapper,
                           @Value("${notification.ws.allowed-origins:*}") String[] allowedOrigins) {
        this.objectMapper = objectMapper;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket nativo, sin SockJS: el gateway proxea con lb:ws:// y SockJS necesitaría además rutas HTTP.
        registry.addEndpoint(ENDPOINT)
                .addInterceptors(new RequireUserHeaderInterceptor())
                .setHandshakeHandler(new HeaderUserHandshakeHandler())
                .setAllowedOriginPatterns(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue")
                // Heartbeats cada 10 s: detectan conexiones muertas y evitan que proxies cierren sockets inactivos
                .setHeartbeatValue(new long[]{10_000, 10_000})
                .setTaskScheduler(wsHeartbeatScheduler());
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new StompSecurityInterceptor());
    }

    /** Usa el ObjectMapper de Spring Boot, así las fechas salen en ISO-8601 igual que en la API REST. */
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        messageConverters.add(converter);
        return false; // false = no agregar los conversores por defecto
    }

    @Bean
    public ThreadPoolTaskScheduler wsHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        return scheduler;
    }
}
