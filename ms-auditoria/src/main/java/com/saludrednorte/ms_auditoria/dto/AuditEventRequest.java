package com.saludrednorte.ms_auditoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para registrar un evento de auditoría en el sistema.
 */
public class AuditEventRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @NotBlank(message = "La acción es obligatoria")
    @Size(max = 100, message = "La acción no puede superar 100 caracteres")
    private String action;

    @Size(max = 1000, message = "Los detalles no pueden superar 1000 caracteres")
    private String details;

    public AuditEventRequest() {}

    public AuditEventRequest(String username, String action, String details) {
        this.username = username;
        this.action = action;
        this.details = details;
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
}
