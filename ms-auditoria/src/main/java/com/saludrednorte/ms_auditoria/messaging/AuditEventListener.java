package com.saludrednorte.ms_auditoria.messaging;

import com.saludrednorte.ms_auditoria.dto.AuditEventRequest;
import com.saludrednorte.ms_auditoria.service.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Consumidor RabbitMQ que persiste eventos de auditoría del sistema.
 */
@Component
public class AuditEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuditEventListener.class);

    @Autowired
    private AuditoriaService auditoriaService;

    @RabbitListener(queues = AuditoriaMessagingConstants.QUEUE)
    public void procesarEvento(AuditEventMessage evento) {
        logger.info("Evento de auditoría recibido desde {}: {}", evento.getOrigen(), evento.getAction());

        AuditEventRequest request = new AuditEventRequest(
                evento.getUsername(),
                evento.getAction(),
                evento.getDetails()
        );
        auditoriaService.registrarEvento(request);
    }
}
