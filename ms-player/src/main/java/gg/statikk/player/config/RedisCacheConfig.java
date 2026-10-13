package gg.statikk.player.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gg.statikk.player.dto.PlayerProfileResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Cachea los perfiles de jugador (GET /api/players/{id}) para no pegarle a MySQL
 * en cada lectura. Se invalida explícitamente en cada update/add-game/delete-game.
 */
@Configuration
public class RedisCacheConfig {

    private static final Duration PLAYER_PROFILE_TTL = Duration.ofMinutes(10);

    /**
     * Serializer TIPADO a propósito. La versión inicial (v0.3) usaba GenericJackson2JsonRedisSerializer con un
     * ObjectMapper propio sin "default typing": al LEER de Redis el valor volvía como LinkedHashMap y @Cacheable
     * lanzaba ClassCastException en la segunda lectura del mismo perfil. Con un tipo concreto, Jackson sabe
     * a qué clase deserializar. (Este cache solo guarda PlayerProfileResponse.)
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<PlayerProfileResponse> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, PlayerProfileResponse.class);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(PLAYER_PROFILE_TTL)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
