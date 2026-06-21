package com.saludrednorte.ms_optimizacion.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class AuditEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(AuditEventPublisher.class);
    private static final String ORIGEN = "ms-optimizacion";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publicar(String username, String action, String details) {
        try {
            rabbitTemplate.convertAndSend(
                    AuditoriaMessagingConstants.EXCHANGE,
                    AuditoriaMessagingConstants.ROUTING_KEY,
                    new AuditEventMessage(username, action, details, ORIGEN)
            );
        } catch (Exception e) {
            logger.warn("No se pudo publicar auditoría {}: {}", action, e.getMessage());
        }
    }

    @Configuration
    static class AuditRabbitConfig {
        @Bean
        TopicExchange auditoriaExchange() {
            return new TopicExchange(AuditoriaMessagingConstants.EXCHANGE, true, false);
        }
    }
}
