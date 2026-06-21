package com.saludrednorte.ms_auditoria.exception;

/**
 * Excepción lanzada cuando no se encuentra un registro de auditoría.
 */
public class AuditLogNotFoundException extends RuntimeException {

    public AuditLogNotFoundException(String message) {
        super(message);
    }
}
