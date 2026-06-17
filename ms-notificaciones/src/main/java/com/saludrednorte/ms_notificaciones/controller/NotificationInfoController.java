package com.saludrednorte.ms_notificaciones.controller;

import com.saludrednorte.ms_notificaciones.dto.ChannelInfoDTO;
import com.saludrednorte.ms_notificaciones.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para información del servicio de notificaciones.
 * <p>
 * Proporciona endpoints para obtener información sobre canales disponibles
 * y estado operativo del microservicio.
 * </p>
 */
@RestController
@RequestMapping("/api/notificaciones/info")
@Tag(name = "Información de Notificaciones", description = "API para obtener información del servicio de notificaciones")
public class NotificationInfoController {

    private final NotificationService notificationService;

    /**
     * Constructor del controlador.
     *
     * @param notificationService el servicio de notificaciones
     */
    public NotificationInfoController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Obtiene la lista de canales de notificación disponibles.
     *
     * @return lista de canales disponibles con descripciones
     */
    @GetMapping("/canales")
    @Operation(summary = "Obtener canales disponibles", description = "Retorna la lista de canales de notificación disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de canales obtenida exitosamente")
    public ResponseEntity<List<ChannelInfoDTO>> canalesDisponibles() {
        List<ChannelInfoDTO> channels = notificationService.getAvailableChannels().stream()
                .map(channel -> new ChannelInfoDTO(
                        channel,
                        "Canal de notificación: " + channel
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(channels);
    }

    /**
     * Obtiene el estado operativo del microservicio de notificaciones.
     *
     * @return mensaje de estado del servicio
     */
    @GetMapping("/estado")
    @Operation(summary = "Obtener estado del servicio", description = "Retorna el estado operativo del microservicio de notificaciones")
    @ApiResponse(responseCode = "200", description = "Estado del servicio obtenido exitosamente")
    public ResponseEntity<String> estado() {
        return ResponseEntity.ok("Microservicio de notificaciones operacional");
    }
}
