package gg.statikk.stats.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gg.statikk.stats.dto.PlayerStatsResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisCacheConfig {

    public static final String PLAYER_STATS_CACHE = "player-stats";

    private static final Duration TTL = Duration.ofMinutes(5);

    /**
     * Serializer TIPADO a propósito. Con GenericJackson2JsonRedisSerializer y un ObjectMapper propio
     * (sin "default typing"), al leer de Redis el valor vuelve como LinkedHashMap y @Cacheable lanza
     * ClassCastException en la segunda lectura. Con un tipo concreto, Jackson sabe a qué clase deserializar.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<PlayerStatsResponse> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, PlayerStatsResponse.class);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(TTL)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
