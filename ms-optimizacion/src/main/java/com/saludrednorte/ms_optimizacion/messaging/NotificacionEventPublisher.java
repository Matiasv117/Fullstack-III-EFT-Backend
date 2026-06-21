package com.saludrednorte.ms_optimizacion.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos de notificación hacia RabbitMQ.
 */
@Component
public class NotificacionEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionEventPublisher.class);
    private static final String ORIGEN = "ms-optimizacion";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Publica un evento de notificación de forma asíncrona.
     */
    public void publicar(Long pacienteId, String tipo, String mensaje) {
        NotificacionEvent evento = new NotificacionEvent(pacienteId, tipo, mensaje, ORIGEN);
        rabbitTemplate.convertAndSend(
                NotificacionMessagingConstants.EXCHANGE,
                NotificacionMessagingConstants.ROUTING_KEY,
                evento
        );
        logger.info("Evento de notificación publicado para paciente {} tipo {}", pacienteId, tipo);
    }
}
