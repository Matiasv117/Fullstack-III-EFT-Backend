package com.saludrednorte.ms_optimizacion.controller;

import com.saludrednorte.ms_optimizacion.entity.Horario;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import com.saludrednorte.ms_optimizacion.service.HorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para la gestión de horarios médicos.
 * <p>
 * Proporciona endpoints para crear, consultar, actualizar y eliminar horarios,
 * incluyendo consulta de horarios disponibles por médico y fecha.
 * </p>
 */
@RestController
@RequestMapping("/horarios")
@Tag(name = "Horarios", description = "API para la gestión de horarios médicos")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    /**
     * Crea un nuevo horario médico.
     *
     * @param horario el horario a crear
     * @return el horario creado con ID asignado
     */
    @PostMapping
    @Operation(summary = "Crear un nuevo horario", description = "Crea un nuevo horario para un médico")
    @ApiResponse(responseCode = "200", description = "Horario creado exitosamente")
    public Horario crearHorario(@RequestBody Horario horario) {
        return horarioService.crearHorario(horario);
    }

    /**
     * Obtiene todos los horarios registrados.
     *
     * @return lista de todos los horarios
     */
    @GetMapping
    @Operation(summary = "Obtener todos los horarios", description = "Retorna una lista con todos los horarios registrados")
    @ApiResponse(responseCode = "200", description = "Lista de horarios obtenida exitosamente")
    public List<Horario> obtenerTodosHorarios() {
        return horarioService.obtenerTodosHorarios();
    }

    /**
     * Obtiene horarios disponibles para un médico en una fecha específica.
     *
     * @param medicoId el ID del médico
     * @param fecha la fecha a consultar
     * @return lista de horarios disponibles
     */
    @GetMapping("/disponibles")
    @Operation(summary = "Obtener horarios disponibles", description = "Retorna horarios disponibles para un médico en una fecha específica")
    @ApiResponse(responseCode = "200", description = "Horarios disponibles obtenidos exitosamente")
    public List<Horario> obtenerHorariosDisponibles(
            @Parameter(description = "ID del médico") @RequestParam Long medicoId,
            @Parameter(description = "Fecha (formato ISO)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        Medico medico = new Medico();
        medico.setId(medicoId);
        return horarioService.obtenerHorariosDisponibles(medico, fecha);
    }

    /**
     * Obtiene un horario por su ID.
     *
     * @param id el ID del horario
     * @return ResponseEntity con el horario si existe, 404 si no existe
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener horario por ID", description = "Retorna un horario específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Horario encontrado"),
            @ApiResponse(responseCode = "404", description = "Horario no encontrado")
    })
    public ResponseEntity<Horario> obtenerHorarioPorId(
            @Parameter(description = "ID del horario") @PathVariable Long id) {
        return horarioService.obtenerHorarioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza un horario existente.
     *
     * @param horario el horario con los datos actualizados
     * @return el horario actualizado
     */
    @PutMapping
    @Operation(summary = "Actualizar horario", description = "Actualiza la información de un horario existente")
    @ApiResponse(responseCode = "200", description = "Horario actualizado exitosamente")
    public Horario actualizarHorario(@RequestBody Horario horario) {
        return horarioService.actualizarHorario(horario);
    }

    /**
     * Elimina un horario del sistema.
     *
     * @param id el ID del horario a eliminar
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar horario", description = "Elimina un horario del sistema")
    @ApiResponse(responseCode = "200", description = "Horario eliminado exitosamente")
    public void eliminarHorario(
            @Parameter(description = "ID del horario") @PathVariable Long id) {
        horarioService.eliminarHorario(id);
    }
}
