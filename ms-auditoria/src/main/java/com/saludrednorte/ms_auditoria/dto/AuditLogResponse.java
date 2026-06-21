package com.saludrednorte.ms_auditoria.dto;

import com.saludrednorte.ms_auditoria.entity.AuditLog;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para un registro de auditoría.
 */
public class AuditLogResponse {

    private Long id;
    private String username;
    private String action;
    private String details;
    private LocalDateTime timestamp;

    public AuditLogResponse() {}

    public AuditLogResponse(Long id, String username, String action, String details, LocalDateTime timestamp) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
    }

    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUsername(),
                auditLog.getAction(),
                auditLog.getDetails(),
                auditLog.getTimestamp()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
