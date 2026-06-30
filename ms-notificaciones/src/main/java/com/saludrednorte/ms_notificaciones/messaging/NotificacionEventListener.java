package com.saludrednorte.ms_notificaciones.messaging;

import com.saludrednorte.ms_notificaciones.entity.Notification;
import com.saludrednorte.ms_notificaciones.entity.TipoNotificacion;
import com.saludrednorte.ms_notificaciones.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Consumidor RabbitMQ que procesa eventos de notificación de forma asíncrona.
 */
@Component
public class NotificacionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionEventListener.class);

    @Autowired
    private NotificationService notificationService;

    /**
     * Procesa un evento de notificación recibido desde la cola RabbitMQ.
     *
     * @param evento evento de notificación publicado por otro microservicio
     */
    @RabbitListener(queues = NotificacionMessagingConstants.QUEUE)
    public void procesarEvento(NotificacionEvent evento) {
        logger.info("Evento recibido desde {} para paciente {}", evento.getOrigen(), evento.getPacienteId());

        Notification notification = new Notification();
        notification.setPacienteId(evento.getPacienteId());
        notification.setTipo(mapTipo(evento.getTipo()));
        notification.setMensaje(evento.getMensaje());
        notification.setEmailDestino(evento.getEmailDestino());

        Notification creada = notificationService.create(notification);
        notificationService.sendById(creada.getId());

        logger.info("Notificación {} creada y enviada desde evento RabbitMQ", creada.getId());
    }

    private TipoNotificacion mapTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return TipoNotificacion.ACTUALIZACION_ESTADO;
        }

        try {
            return TipoNotificacion.valueOf(tipo);
        } catch (IllegalArgumentException ex) {
            if ("CITA_REASIGNADA".equalsIgnoreCase(tipo)) {
                return TipoNotificacion.CAMBIO_HORARIO;
            }
            return TipoNotificacion.ACTUALIZACION_ESTADO;
        }
    }
}
