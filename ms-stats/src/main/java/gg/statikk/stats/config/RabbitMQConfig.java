package gg.statikk.stats.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RabbitMQConfig {

    public static final String EVENTS_EXCHANGE = "statikk.events";

    /** Una sola cola para todo evento que cambie las partidas de un jugador. */
    public static final String MATCH_EVENTS_QUEUE = "ms-stats.match-events";

    private static final List<String> MATCH_ROUTING_KEYS = List.of(
            "match.created",
            "match.deleted",
            "match.bulk.imported",
            "match.bulk.deleted");

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue matchEventsQueue() {
        return QueueBuilder.durable(MATCH_EVENTS_QUEUE).build();
    }

    @Bean
    public Declarables matchEventBindings(Queue matchEventsQueue, TopicExchange eventsExchange) {
        List<Binding> bindings = MATCH_ROUTING_KEYS.stream()
                .map(key -> BindingBuilder.bind(matchEventsQueue).to(eventsExchange).with(key))
                .toList();
        return new Declarables(bindings);
    }

    /** El tipo se infiere del parámetro del @RabbitListener, no de la clase del servicio que publicó. */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
