package com.saludrednorte.ms_notificaciones.config;

import com.saludrednorte.ms_notificaciones.messaging.NotificacionMessagingConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para consumo de eventos de notificación.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange notificacionesExchange() {
        return new TopicExchange(NotificacionMessagingConstants.EXCHANGE, true, false);
    }

    @Bean
    public Queue notificacionesQueue() {
        return new Queue(NotificacionMessagingConstants.QUEUE, true);
    }

    @Bean
    public Binding notificacionesBinding(Queue notificacionesQueue, TopicExchange notificacionesExchange) {
        return BindingBuilder.bind(notificacionesQueue)
                .to(notificacionesExchange)
                .with(NotificacionMessagingConstants.ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
