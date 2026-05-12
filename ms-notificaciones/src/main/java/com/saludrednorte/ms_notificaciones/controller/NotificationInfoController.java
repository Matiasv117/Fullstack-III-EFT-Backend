package com.saludrednorte.ms_notificaciones.controller;

import com.saludrednorte.ms_notificaciones.dto.ChannelInfoDTO;
import com.saludrednorte.ms_notificaciones.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Información del servicio de notificaciones (rutas en español).
 */
@RestController
@RequestMapping("/api/notificaciones/info")
public class NotificationInfoController {

    private final NotificationService notificationService;

    public NotificationInfoController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/canales")
    public ResponseEntity<List<ChannelInfoDTO>> canalesDisponibles() {
        List<ChannelInfoDTO> channels = notificationService.getAvailableChannels().stream()
                .map(channel -> new ChannelInfoDTO(
                        channel,
                        "Canal de notificación: " + channel
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/estado")
    public ResponseEntity<String> estado() {
        return ResponseEntity.ok("Microservicio de notificaciones operacional");
    }
}
