package com.saludrednorte.ms_progreso.controller;

import com.saludrednorte.ms_progreso.dto.ProgresoRequest;
import com.saludrednorte.ms_progreso.dto.ProgresoResponse;
import com.saludrednorte.ms_progreso.service.ProgresoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints para consultar y actualizar el progreso del paciente.
 */
@RestController
@RequestMapping("/progreso")
public class ProgresoController {

    @Autowired
    private ProgresoService progresoService;

    /**
     * Registra el estado inicial del paciente.
     */
    @PostMapping("/pacientes/{pacienteId}")
    public ProgresoResponse registrar(@PathVariable Long pacienteId, @Valid @RequestBody ProgresoRequest request) {
        return progresoService.registrarProgreso(pacienteId, request);
    }

    /**
     * Actualiza el estado del paciente.
     */
    @PutMapping("/pacientes/{pacienteId}")
    public ProgresoResponse actualizar(@PathVariable Long pacienteId, @Valid @RequestBody ProgresoRequest request) {
        return progresoService.actualizarProgreso(pacienteId, request);
    }

    /**
     * Obtiene el estado actual del paciente.
     */
    @GetMapping("/pacientes/{pacienteId}")
    public ProgresoResponse obtener(@PathVariable Long pacienteId) {
        return progresoService.obtenerProgreso(pacienteId);
    }
}

