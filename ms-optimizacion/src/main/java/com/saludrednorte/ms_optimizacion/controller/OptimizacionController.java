package com.saludrednorte.ms_optimizacion.controller;

import com.saludrednorte.ms_optimizacion.dto.ListaEsperaDTO;
import com.saludrednorte.ms_optimizacion.dto.PrioridadResponse;
import com.saludrednorte.ms_optimizacion.dto.ReasignacionResponse;
import com.saludrednorte.ms_optimizacion.service.OptimizacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la optimización de citas.
 * <p>
 * Proporciona endpoints para procesar cancelaciones, obtener lista de espera
 * y calcular prioridad de pacientes para reasignación automática.
 * </p>
 */
@RestController
@RequestMapping("/optimizacion")
@Tag(name = "Optimización", description = "API para la optimización de citas y reasignación automática")
public class OptimizacionController {

    @Autowired
    private OptimizacionService optimizacionService;

    /**
     * Procesa la cancelación de una cita y reasigna automáticamente.
     *
     * @param citaId el ID de la cita a cancelar
     * @param estrategia la estrategia de reasignación (fifo, prioridad)
     */
    @PostMapping("/cancelar/{citaId}")
    @Operation(summary = "Procesar cancelación de cita", description = "Procesa la cancelación de una cita y reasigna automáticamente según la estrategia especificada")
    @ApiResponse(responseCode = "200", description = "Cancelación procesada exitosamente")
    public ReasignacionResponse procesarCancelacion(
            @Parameter(description = "ID de la cita a cancelar") @PathVariable Long citaId,
            @Parameter(description = "Estrategia de reasignación (fifo, prioridad)") @RequestParam(defaultValue = "fifo") String estrategia) {
        return optimizacionService.procesarCancelacion(citaId, estrategia);
    }

    /**
     * Obtiene la lista de espera de pacientes para reasignación.
     *
     * @return lista de pacientes en espera
     */
    @GetMapping("/lista-espera")
    @Operation(summary = "Obtener lista de espera", description = "Retorna la lista de espera de pacientes para reasignación")
    @ApiResponse(responseCode = "200", description = "Lista de espera obtenida exitosamente")
    public List<ListaEsperaDTO> obtenerListaEspera() {
        return optimizacionService.obtenerListaEspera();
    }

    /**
     * Calcula la prioridad de un paciente según gravedad, distancia y días de espera.
     *
     * @param gravedad nivel 1-5
     * @param distanciaKm distancia geográfica en kilómetros
     * @param diasEspera días acumulados en espera
     * @return respuesta con el nivel de prioridad calculado
     */
    @GetMapping("/prioridad")
    @Operation(summary = "Calcular prioridad de paciente", description = "Calcula el nivel de prioridad de un paciente basado en gravedad, distancia y días de espera")
    @ApiResponse(responseCode = "200", description = "Prioridad calculada exitosamente")
    public PrioridadResponse calcularPrioridad(
            @Parameter(description = "Nivel de gravedad (1-5)") @RequestParam int gravedad,
            @Parameter(description = "Distancia en kilómetros") @RequestParam double distanciaKm,
            @Parameter(description = "Días acumulados en espera") @RequestParam int diasEspera) {
        return new PrioridadResponse(
                optimizacionService.calcularPrioridadPaciente(gravedad, distanciaKm, diasEspera).name()
        );
    }
}
