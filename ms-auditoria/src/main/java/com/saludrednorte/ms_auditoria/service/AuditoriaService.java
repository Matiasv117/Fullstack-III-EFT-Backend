package com.saludrednorte.ms_auditoria.service;

import com.saludrednorte.ms_auditoria.dto.AuditEventRequest;
import com.saludrednorte.ms_auditoria.dto.AuditLogResponse;
import com.saludrednorte.ms_auditoria.entity.AuditLog;
import com.saludrednorte.ms_auditoria.exception.AuditLogNotFoundException;
import com.saludrednorte.ms_auditoria.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de negocio para registrar y consultar eventos de auditoría del sistema.
 */
@Service
public class AuditoriaService {

    public static final String PACIENTE_REGISTRADO = "PACIENTE_REGISTRADO";
    public static final String CITA_OPTIMIZADA = "CITA_OPTIMIZADA";
    public static final String LOGIN_EXITOSO = "LOGIN_EXITOSO";
    public static final String LOGIN_FALLIDO = "LOGIN_FALLIDO";

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Registra un nuevo evento de auditoría.
     *
     * @param request datos del evento
     * @return registro de auditoría persistido
     */
    public AuditLogResponse registrarEvento(AuditEventRequest request) {
        AuditLog auditLog = new AuditLog(
                request.getUsername(),
                request.getAction().toUpperCase(),
                request.getDetails()
        );
        return AuditLogResponse.fromEntity(auditLogRepository.save(auditLog));
    }

    /**
     * Obtiene todos los registros de auditoría.
     *
     * @return lista de eventos registrados
     */
    public List<AuditLogResponse> listarTodos() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    /**
     * Obtiene un registro de auditoría por su identificador.
     *
     * @param id identificador del evento
     * @return registro encontrado
     */
    public AuditLogResponse obtenerPorId(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new AuditLogNotFoundException("Registro de auditoría no encontrado: " + id));
        return AuditLogResponse.fromEntity(auditLog);
    }

    /**
     * Obtiene eventos de auditoría filtrados por usuario.
     *
     * @param username nombre del usuario
     * @return eventos del usuario
     */
    public List<AuditLogResponse> listarPorUsuario(String username) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username).stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    /**
     * Obtiene eventos de auditoría filtrados por tipo de acción.
     *
     * @param action acción registrada
     * @return eventos de la acción indicada
     */
    public List<AuditLogResponse> listarPorAccion(String action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action.toUpperCase()).stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }
}
