package com.saludrednorte.ms_auditoria.config;

import com.saludrednorte.ms_auditoria.messaging.AuditoriaMessagingConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración RabbitMQ para consumo de eventos de auditoría.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange auditoriaExchange() {
        return new TopicExchange(AuditoriaMessagingConstants.EXCHANGE, true, false);
    }

    @Bean
    public Queue auditoriaQueue() {
        return new Queue(AuditoriaMessagingConstants.QUEUE, true);
    }

    @Bean
    public Binding auditoriaBinding(Queue auditoriaQueue, TopicExchange auditoriaExchange) {
        return BindingBuilder.bind(auditoriaQueue)
                .to(auditoriaExchange)
                .with(AuditoriaMessagingConstants.ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
