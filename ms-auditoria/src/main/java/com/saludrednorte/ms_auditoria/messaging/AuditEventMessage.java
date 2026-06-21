package com.saludrednorte.ms_auditoria.messaging;

/**
 * Evento de auditoría recibido desde otros microservicios.
 */
public class AuditEventMessage {

    private String username;
    private String action;
    private String details;
    private String origen;

    public AuditEventMessage() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }
}
