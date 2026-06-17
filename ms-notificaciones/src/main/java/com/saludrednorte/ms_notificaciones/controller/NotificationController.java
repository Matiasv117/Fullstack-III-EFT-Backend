package com.saludrednorte.ms_notificaciones.controller;

import com.saludrednorte.ms_notificaciones.dto.NotificationMapper;
import com.saludrednorte.ms_notificaciones.dto.NotificationRequestDTO;
import com.saludrednorte.ms_notificaciones.dto.NotificationResponseDTO;
import com.saludrednorte.ms_notificaciones.entity.Notification;
import com.saludrednorte.ms_notificaciones.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de notificaciones.
 * <p>
 * Proporciona endpoints para crear, consultar y enviar notificaciones
 * por diferentes canales (EMAIL, SMS, PUSH).
 * </p>
 */
@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "API para la gestión de notificaciones del sistema")
public class NotificationController {

    private final NotificationService service;
    private final NotificationMapper mapper;

    /**
     * Constructor del controlador.
     *
     * @param service el servicio de notificaciones
     * @param mapper el mapeador de DTOs
     */
    public NotificationController(NotificationService service, NotificationMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * Crea una nueva notificación en el sistema.
     *
     * @param request la solicitud de notificación
     * @return la notificación creada
     */
    @PostMapping
    @Operation(summary = "Crear notificación", description = "Crea una nueva notificación en el sistema")
    @ApiResponse(responseCode = "200", description = "Notificación creada exitosamente")
    public ResponseEntity<NotificationResponseDTO> create(@Valid @RequestBody NotificationRequestDTO request) {
        Notification saved = service.create(mapper.requestDtoToEntity(request));
        return ResponseEntity.ok(mapper.entityToResponseDto(saved));
    }

    /**
     * Obtiene todas las notificaciones pendientes de envío.
     *
     * @return lista de notificaciones pendientes
     */
    @GetMapping("/pendientes")
    @Operation(summary = "Obtener notificaciones pendientes", description = "Retorna todas las notificaciones pendientes de envío")
    @ApiResponse(responseCode = "200", description = "Lista de notificaciones pendientes obtenida exitosamente")
    public ResponseEntity<List<NotificationResponseDTO>> pendientes() {
        List<NotificationResponseDTO> result = service.findPending().stream()
                .map(mapper::entityToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Obtiene una notificación por su ID.
     *
     * @param id el ID de la notificación
     * @return ResponseEntity con la notificación si existe, 404 si no existe
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener notificación por ID", description = "Retorna una notificación específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación encontrada"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    public ResponseEntity<NotificationResponseDTO> getById(
            @Parameter(description = "ID de la notificación") @PathVariable Long id) {
        return service.findById(id)
                .map(mapper::entityToResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene todas las notificaciones de un paciente específico.
     *
     * @param pacienteId el ID del paciente
     * @return lista de notificaciones del paciente
     */
    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Obtener notificaciones por paciente", description = "Retorna todas las notificaciones de un paciente específico")
    @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida exitosamente")
    public ResponseEntity<List<NotificationResponseDTO>> getByPacienteId(
            @Parameter(description = "ID del paciente") @PathVariable Long pacienteId) {
        List<NotificationResponseDTO> result = service.findByPacienteId(pacienteId).stream()
                .map(mapper::entityToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Envía una notificación específica.
     *
     * @param id el ID de la notificación
     * @return ResponseEntity con 200 si se envió, 404 si no existe
     */
    @PostMapping("/{id}/enviar")
    @Operation(summary = "Enviar notificación", description = "Envía una notificación específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación enviada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    public ResponseEntity<Void> enviar(
            @Parameter(description = "ID de la notificación") @PathVariable Long id) {
        return service.sendById(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Envía una notificación por un canal específico.
     *
     * @param id el ID de la notificación
     * @param canal el canal de envío (EMAIL, SMS, PUSH)
     * @return ResponseEntity con 200 si se envió, 404 si no existe
     */
    @PostMapping("/{id}/enviar-canal")
    @Operation(summary = "Enviar notificación por canal específico", description = "Envía una notificación por un canal específico (EMAIL, SMS, PUSH)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación enviada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    public ResponseEntity<Void> enviarPorCanal(
            @Parameter(description = "ID de la notificación") @PathVariable Long id,
            @Parameter(description = "Canal de envío (EMAIL, SMS, PUSH)") @RequestParam("canal") String canal) {
        return service.sendById(id, canal) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Envía todas las notificaciones pendientes.
     *
     * @return ResponseEntity con 200 si se enviaron
     */
    @PostMapping("/enviar-todas")
    @Operation(summary = "Enviar todas las notificaciones pendientes", description = "Envía todas las notificaciones que están pendientes")
    @ApiResponse(responseCode = "200", description = "Notificaciones enviadas exitosamente")
    public ResponseEntity<Void> enviarTodas() {
        service.sendPending();
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene todas las notificaciones del sistema.
     *
     * @return lista de todas las notificaciones
     */
    @GetMapping
    @Operation(summary = "Listar todas las notificaciones", description = "Retorna todas las notificaciones del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida exitosamente")
    public ResponseEntity<List<NotificationResponseDTO>> listar() {
        List<NotificationResponseDTO> result = service.findAll().stream()
                .map(mapper::entityToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
