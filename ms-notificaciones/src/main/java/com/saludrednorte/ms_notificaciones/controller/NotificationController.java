package com.saludrednorte.ms_notificaciones.controller;

import com.saludrednorte.ms_notificaciones.dto.NotificationMapper;
import com.saludrednorte.ms_notificaciones.dto.NotificationRequestDTO;
import com.saludrednorte.ms_notificaciones.dto.NotificationResponseDTO;
import com.saludrednorte.ms_notificaciones.entity.Notification;
import com.saludrednorte.ms_notificaciones.service.NotificationService;
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

@RestController
@RequestMapping("/api/notificaciones")
public class NotificationController {

    private final NotificationService service;
    private final NotificationMapper mapper;

    public NotificationController(NotificationService service, NotificationMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<NotificationResponseDTO> create(@Valid @RequestBody NotificationRequestDTO request) {
        Notification saved = service.create(mapper.requestDtoToEntity(request));
        return ResponseEntity.ok(mapper.entityToResponseDto(saved));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<NotificationResponseDTO>> pendientes() {
        List<NotificationResponseDTO> result = service.findPending().stream()
                .map(mapper::entityToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::entityToResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<NotificationResponseDTO>> getByPacienteId(@PathVariable Long pacienteId) {
        List<NotificationResponseDTO> result = service.findByPacienteId(pacienteId).stream()
                .map(mapper::entityToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<Void> enviar(@PathVariable Long id) {
        return service.sendById(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/enviar-canal")
    public ResponseEntity<Void> enviarPorCanal(@PathVariable Long id, @RequestParam("canal") String canal) {
        return service.sendById(id, canal) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/enviar-todas")
    public ResponseEntity<Void> enviarTodas() {
        service.sendPending();
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> listar() {
        List<NotificationResponseDTO> result = service.findAll().stream()
                .map(mapper::entityToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
