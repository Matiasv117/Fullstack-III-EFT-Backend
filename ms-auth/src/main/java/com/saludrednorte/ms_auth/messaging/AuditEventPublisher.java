package com.saludrednorte.ms_auth.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(AuditEventPublisher.class);
    private static final String ORIGEN = "ms-auth";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publicar(String username, String action, String details) {
        try {
            AuditEventMessage evento = new AuditEventMessage(username, action, details, ORIGEN);
            rabbitTemplate.convertAndSend(
                    AuditoriaMessagingConstants.EXCHANGE,
                    AuditoriaMessagingConstants.ROUTING_KEY,
                    evento
            );
            logger.debug("Evento de auditoría publicado: {}", action);
        } catch (Exception e) {
            logger.warn("No se pudo publicar evento de auditoría {}: {}", action, e.getMessage());
        }
    }
}
