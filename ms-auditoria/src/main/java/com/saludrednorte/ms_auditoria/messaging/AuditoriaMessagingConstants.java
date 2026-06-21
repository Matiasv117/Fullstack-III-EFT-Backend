package com.saludrednorte.ms_auditoria.messaging;

/**
 * Contrato RabbitMQ para eventos de auditoría.
 */
public final class AuditoriaMessagingConstants {

    public static final String EXCHANGE = "salud.auditoria.exchange";
    public static final String QUEUE = "salud.auditoria.queue";
    public static final String ROUTING_KEY = "auditoria.evento";

    private AuditoriaMessagingConstants() {
    }
}
