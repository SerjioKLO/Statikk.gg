package gg.statikk.matches.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
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

@Configuration
public class RabbitMQConfig {

    /** Mismo exchange compartido que declara ms-player; RabbitMQ no duplica si ya existe con la misma config. */
    public static final String EVENTS_EXCHANGE = "statikk.events";

    /** Cola propia de ms-matches para las importaciones masivas que publica ms-excel-parser. */
    public static final String EXCEL_IMPORT_QUEUE = "ms-matches.excel-imports";
    public static final String EXCEL_IMPORT_ROUTING_KEY = "excel.matches.imported";

    /** Cola propia de ms-matches para el borrado en cascada cuando un jugador elimina un videojuego. */
    public static final String PLAYER_GAME_DELETED_QUEUE = "ms-matches.player-game-deleted";
    public static final String PLAYER_GAME_DELETED_ROUTING_KEY = "player.game.deleted";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue excelImportQueue() {
        return QueueBuilder.durable(EXCEL_IMPORT_QUEUE).build();
    }

    @Bean
    public Binding excelImportBinding(Queue excelImportQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(excelImportQueue).to(eventsExchange).with(EXCEL_IMPORT_ROUTING_KEY);
    }

    @Bean
    public Queue playerGameDeletedQueue() {
        return QueueBuilder.durable(PLAYER_GAME_DELETED_QUEUE).build();
    }

    @Bean
    public Binding playerGameDeletedBinding(Queue playerGameDeletedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(playerGameDeletedQueue).to(eventsExchange).with(PLAYER_GAME_DELETED_ROUTING_KEY);
    }

    /**
     * Por defecto el mensaje trae el nombre de la clase del PRODUCTOR en su cabecera
     * (gg.statikk.excel.event.ExcelMatchesImportedEvent), que no existe en este servicio.
     * Con INFERRED, el tipo se deduce del parámetro del método @RabbitListener.
     */
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
