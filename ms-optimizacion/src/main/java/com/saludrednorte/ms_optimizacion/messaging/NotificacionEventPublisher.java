package com.saludrednorte.ms_optimizacion.messaging;

import com.saludrednorte.ms_optimizacion.client.NotificationClient;
import com.saludrednorte.ms_optimizacion.dto.NotificationRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificacionEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionEventPublisher.class);

    @Autowired
    private NotificationClient notificationClient;

    public void publicar(Long pacienteId, String tipo, String mensaje) {
        publicar(pacienteId, tipo, mensaje, null);
    }

    public void publicar(Long pacienteId, String tipo, String mensaje, String emailDestino) {
        try {
            NotificationRequestDTO request = new NotificationRequestDTO(pacienteId, tipo, mensaje);
            notificationClient.createNotification(request);
            logger.info("Notificación enviada vía Feign para paciente {} tipo {}", pacienteId, tipo);
        } catch (Exception e) {
            logger.warn("Fallo al enviar notificación vía Feign: {}", e.getMessage());
        }
    }
}
