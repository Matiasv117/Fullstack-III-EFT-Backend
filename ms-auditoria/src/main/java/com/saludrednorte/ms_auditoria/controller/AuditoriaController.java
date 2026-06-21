package com.saludrednorte.ms_auditoria.controller;

import com.saludrednorte.ms_auditoria.dto.AuditEventRequest;
import com.saludrednorte.ms_auditoria.dto.AuditLogResponse;
import com.saludrednorte.ms_auditoria.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para registrar y consultar eventos de auditoría del sistema de salud.
 */
@RestController
@RequestMapping("/api/auditoria")
@Tag(name = "Auditoría", description = "Registro y consulta de eventos del sistema RedNorte")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    /**
     * Registra un nuevo evento de auditoría.
     *
     * @param request datos del evento
     * @return evento registrado
     */
    @PostMapping("/eventos")
    @Operation(summary = "Registrar evento", description = "Persiste un evento de auditoría en el sistema")
    public ResponseEntity<AuditLogResponse> registrarEvento(@Valid @RequestBody AuditEventRequest request) {
        AuditLogResponse response = auditoriaService.registrarEvento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista todos los eventos de auditoría registrados.
     *
     * @return lista de eventos
     */
    @GetMapping("/eventos")
    @Operation(summary = "Listar eventos", description = "Obtiene todos los registros de auditoría")
    public List<AuditLogResponse> listarEventos() {
        return auditoriaService.listarTodos();
    }

    /**
     * Obtiene un evento de auditoría por identificador.
     *
     * @param id identificador del evento
     * @return evento encontrado
     */
    @GetMapping("/eventos/{id}")
    @Operation(summary = "Obtener evento", description = "Consulta un registro de auditoría por ID")
    public AuditLogResponse obtenerEvento(@PathVariable Long id) {
        return auditoriaService.obtenerPorId(id);
    }

    /**
     * Lista eventos de auditoría filtrados por usuario.
     *
     * @param username nombre del usuario
     * @return eventos del usuario
     */
    @GetMapping("/eventos/usuario/{username}")
    @Operation(summary = "Eventos por usuario", description = "Filtra registros de auditoría por nombre de usuario")
    public List<AuditLogResponse> listarPorUsuario(@PathVariable String username) {
        return auditoriaService.listarPorUsuario(username);
    }

    /**
     * Lista eventos de auditoría filtrados por acción.
     *
     * @param action tipo de acción registrada
     * @return eventos de la acción indicada
     */
    @GetMapping("/eventos/accion/{action}")
    @Operation(summary = "Eventos por acción", description = "Filtra registros por tipo de acción")
    public List<AuditLogResponse> listarPorAccion(@PathVariable String action) {
        return auditoriaService.listarPorAccion(action);
    }
}
