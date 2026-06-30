package com.saludrednorte.ms_optimizacion.controller;

import com.saludrednorte.ms_optimizacion.entity.Cita;
import com.saludrednorte.ms_optimizacion.entity.EstadoCita;
import com.saludrednorte.ms_optimizacion.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de citas médicas.
 * <p>
 * Proporciona endpoints para crear, consultar, actualizar y cancelar citas.
 * Incluye validaciones de disponibilidad de médicos.
 * </p>
 */
@RestController
@RequestMapping("/citas")
@Tag(name = "Citas", description = "API para la gestión de citas médicas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    /**
     * Crea una nueva cita médica.
     *
     * @param cita la cita a crear
     * @return la cita creada con ID asignado
     */
    @PostMapping
    @Operation(summary = "Crear una nueva cita", description = "Crea una nueva cita médica en el sistema")
    @ApiResponse(responseCode = "200", description = "Cita creada exitosamente")
    public Cita crearCita(@RequestBody Cita cita) {
        return citaService.crearCita(cita);
    }

    /**
     * Obtiene todas las citas registradas.
     *
     * @return lista de todas las citas
     */
    @GetMapping
    @Operation(summary = "Obtener todas las citas", description = "Retorna una lista con todas las citas registradas")
    @ApiResponse(responseCode = "200", description = "Lista de citas obtenida exitosamente")
    public List<Cita> obtenerTodasCitas() {
        return citaService.obtenerTodasCitas();
    }

    /**
     * Obtiene citas filtradas por estado.
     *
     * @param estado el estado de las citas a buscar
     * @return lista de citas con el estado especificado
     */
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener citas por estado", description = "Retorna citas filtradas por su estado")
    @ApiResponse(responseCode = "200", description = "Lista filtrada obtenida exitosamente")
    public List<Cita> obtenerCitasPorEstado(
            @Parameter(description = "Estado de la cita") @PathVariable EstadoCita estado) {
        return citaService.obtenerCitasPorEstado(estado);
    }

    /**
     * Obtiene las citas de un paciente específico.
     *
     * @param pacienteId el ID del paciente
     * @return lista de citas del paciente
     */
    @GetMapping("/paciente/{pacienteId}")
    @Operation(summary = "Obtener citas por paciente", description = "Retorna las citas de un paciente específico")
    @ApiResponse(responseCode = "200", description = "Citas del paciente obtenidas exitosamente")
    public List<Cita> obtenerCitasPorPaciente(
            @Parameter(description = "ID del paciente") @PathVariable Long pacienteId) {
        return citaService.obtenerCitasPorPaciente(pacienteId);
    }

    /**
     * Obtiene una cita por su ID.
     *
     * @param id el ID de la cita
     * @return ResponseEntity con la cita si existe, 404 si no existe
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener cita por ID", description = "Retorna una cita específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cita encontrada"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    public ResponseEntity<Cita> obtenerCitaPorId(
            @Parameter(description = "ID de la cita") @PathVariable Long id) {
        return citaService.obtenerCitaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza una cita existente.
     *
     * @param cita la cita con los datos actualizados
     * @return la cita actualizada
     */
    @PutMapping
    @Operation(summary = "Actualizar cita", description = "Actualiza la información de una cita existente")
    @ApiResponse(responseCode = "200", description = "Cita actualizada exitosamente")
    public Cita actualizarCita(@RequestBody Cita cita) {
        return citaService.actualizarCita(cita);
    }

    /**
     * Cancela una cita existente.
     *
     * @param id el ID de la cita a cancelar
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar cita", description = "Cancela una cita existente")
    @ApiResponse(responseCode = "200", description = "Cita cancelada exitosamente")
    public void cancelarCita(
            @Parameter(description = "ID de la cita") @PathVariable Long id) {
        citaService.cancelarCita(id);
    }
}
