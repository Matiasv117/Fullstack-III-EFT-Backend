package com.saludrednorte.ms_optimizacion.config;

import com.saludrednorte.ms_optimizacion.messaging.NotificacionMessagingConstants;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para publicación de eventos de notificación.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange notificacionesExchange() {
        return new TopicExchange(NotificacionMessagingConstants.EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
