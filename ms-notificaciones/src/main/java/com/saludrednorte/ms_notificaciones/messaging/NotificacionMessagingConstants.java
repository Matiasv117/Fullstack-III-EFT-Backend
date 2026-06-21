package com.saludrednorte.ms_notificaciones.messaging;

/**
 * Contrato de mensajería RabbitMQ para eventos de notificación.
 */
public final class NotificacionMessagingConstants {

    public static final String EXCHANGE = "salud.notificaciones.exchange";
    public static final String QUEUE = "salud.notificaciones.queue";
    public static final String ROUTING_KEY = "notificacion.evento";

    private NotificacionMessagingConstants() {
    }
}
