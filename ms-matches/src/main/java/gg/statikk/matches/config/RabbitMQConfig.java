package gg.statikk.matches.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "statikk.exchange";

    public static final String EXCEL_MATCHES_IMPORTED_QUEUE = "excel.matches.imported.matches-service.queue";
    public static final String EXCEL_MATCHES_IMPORTED_ROUTING_KEY = "excel.matches.imported";

    public static final String GAME_DELETED_MATCHES_QUEUE = "player.game.deleted.matches-service.queue";
    public static final String GAME_DELETED_ROUTING_KEY = "player.game.deleted";

    public static final String MATCHES_UPDATED_ROUTING_KEY = "matches.updated";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue excelMatchesImportedQueue() {
        return QueueBuilder.durable(EXCEL_MATCHES_IMPORTED_QUEUE).build();
    }

    @Bean
    public Binding excelMatchesImportedBinding(Queue excelMatchesImportedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(excelMatchesImportedQueue).to(exchange).with(EXCEL_MATCHES_IMPORTED_ROUTING_KEY);
    }

    @Bean
    public Queue gameDeletedMatchesQueue() {
        return QueueBuilder.durable(GAME_DELETED_MATCHES_QUEUE).build();
    }

    @Bean
    public Binding gameDeletedMatchesBinding(Queue gameDeletedMatchesQueue, TopicExchange exchange) {
        return BindingBuilder.bind(gameDeletedMatchesQueue).to(exchange).with(GAME_DELETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
