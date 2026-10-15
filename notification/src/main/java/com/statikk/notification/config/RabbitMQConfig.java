package com.statikk.notification.config;

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

    public static final String EXCEL_IMPORTED_NOTIFICATION_QUEUE = "excel.imported.notification-service.queue";
    public static final String STATS_CALCULATED_NOTIFICATION_QUEUE = "stats.calculated.notification-service.queue";
    public static final String MATCHES_UPDATED_NOTIFICATION_QUEUE = "matches.updated.notification-service.queue";

    public static final String EXCEL_IMPORTED_ROUTING_KEY = "excel.matches.imported";
    public static final String STATS_CALCULATED_ROUTING_KEY = "stats.calculated";
    public static final String MATCHES_UPDATED_ROUTING_KEY = "matches.updated";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue excelImportedNotificationQueue() {
        return QueueBuilder.durable(EXCEL_IMPORTED_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding excelImportedNotificationBinding(Queue excelImportedNotificationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(excelImportedNotificationQueue).to(exchange).with(EXCEL_IMPORTED_ROUTING_KEY);
    }

    @Bean
    public Queue statsCalculatedNotificationQueue() {
        return QueueBuilder.durable(STATS_CALCULATED_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding statsCalculatedNotificationBinding(Queue statsCalculatedNotificationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(statsCalculatedNotificationQueue).to(exchange).with(STATS_CALCULATED_ROUTING_KEY);
    }

    @Bean
    public Queue matchesUpdatedNotificationQueue() {
        return QueueBuilder.durable(MATCHES_UPDATED_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding matchesUpdatedNotificationBinding(Queue matchesUpdatedNotificationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(matchesUpdatedNotificationQueue).to(exchange).with(MATCHES_UPDATED_ROUTING_KEY);
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
