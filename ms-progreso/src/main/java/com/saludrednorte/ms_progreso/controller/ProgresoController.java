package com.saludrednorte.ms_progreso.controller;

import com.saludrednorte.ms_progreso.dto.ProgresoRequest;
import com.saludrednorte.ms_progreso.dto.ProgresoResponse;
import com.saludrednorte.ms_progreso.service.ProgresoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión del progreso del paciente.
 * <p>
 * Proporciona endpoints para registrar, actualizar y consultar el estado
 * de progreso de los pacientes en su tratamiento.
 * </p>
 */
@RestController
@RequestMapping("/progreso")
@Tag(name = "Progreso del Paciente", description = "API para la gestión del estado de progreso del paciente")
public class ProgresoController {

    @Autowired
    private ProgresoService progresoService;

    /**
     * Registra el estado inicial del progreso del paciente.
     *
     * @param pacienteId el ID del paciente
     * @param request la solicitud de registro de progreso
     * @return el progreso registrado
     */
    @PostMapping("/pacientes/{pacienteId}")
    @Operation(summary = "Registrar progreso inicial", description = "Registra el estado inicial del progreso del paciente")
    @ApiResponse(responseCode = "200", description = "Progreso registrado exitosamente")
    public ProgresoResponse registrar(
            @Parameter(description = "ID del paciente") @PathVariable Long pacienteId,
            @Valid @RequestBody ProgresoRequest request) {
        return progresoService.registrarProgreso(pacienteId, request);
    }

    /**
     * Actualiza el estado del progreso del paciente.
     *
     * @param pacienteId el ID del paciente
     * @param request la solicitud de actualización de progreso
     * @return el progreso actualizado
     */
    @PutMapping("/pacientes/{pacienteId}")
    @Operation(summary = "Actualizar progreso", description = "Actualiza el estado del progreso del paciente")
    @ApiResponse(responseCode = "200", description = "Progreso actualizado exitosamente")
    public ProgresoResponse actualizar(
            @Parameter(description = "ID del paciente") @PathVariable Long pacienteId,
            @Valid @RequestBody ProgresoRequest request) {
        return progresoService.actualizarProgreso(pacienteId, request);
    }

    /**
     * Obtiene el estado actual del progreso del paciente.
     *
     * @param pacienteId el ID del paciente
     * @return el progreso actual del paciente
     */
    @GetMapping("/pacientes/{pacienteId}")
    @Operation(summary = "Obtener progreso", description = "Obtiene el estado actual del progreso del paciente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progreso obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Progreso no encontrado")
    })
    public ProgresoResponse obtener(
            @Parameter(description = "ID del paciente") @PathVariable Long pacienteId) {
        return progresoService.obtenerProgreso(pacienteId);
    }
}

