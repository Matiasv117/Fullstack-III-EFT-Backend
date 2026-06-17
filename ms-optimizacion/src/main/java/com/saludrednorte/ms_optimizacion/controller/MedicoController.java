package com.saludrednorte.ms_optimizacion.controller;

import com.saludrednorte.ms_optimizacion.entity.Medico;
import com.saludrednorte.ms_optimizacion.service.MedicoService;
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
 * Controlador REST para la gestión de médicos.
 * <p>
 * Proporciona endpoints para registrar, consultar, actualizar y eliminar médicos del sistema.
 * </p>
 */
@RestController
@RequestMapping("/medicos")
@Tag(name = "Médicos", description = "API para la gestión de médicos")
public class MedicoController {

    @Autowired
    private MedicoService medicoService;

    /**
     * Registra un nuevo médico en el sistema.
     *
     * @param medico el médico a registrar
     * @return el médico registrado con ID asignado
     */
    @PostMapping
    @Operation(summary = "Registrar un nuevo médico", description = "Crea un nuevo médico en el sistema")
    @ApiResponse(responseCode = "200", description = "Médico registrado exitosamente")
    public Medico registrarMedico(@RequestBody Medico medico) {
        return medicoService.registrarMedico(medico);
    }

    /**
     * Obtiene todos los médicos registrados.
     *
     * @return lista de todos los médicos
     */
    @GetMapping
    @Operation(summary = "Obtener todos los médicos", description = "Retorna una lista con todos los médicos registrados")
    @ApiResponse(responseCode = "200", description = "Lista de médicos obtenida exitosamente")
    public List<Medico> obtenerTodosMedicos() {
        return medicoService.obtenerTodosMedicos();
    }

    /**
     * Obtiene un médico por su ID.
     *
     * @param id el ID del médico
     * @return ResponseEntity con el médico si existe, 404 si no existe
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener médico por ID", description = "Retorna un médico específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Médico encontrado"),
            @ApiResponse(responseCode = "404", description = "Médico no encontrado")
    })
    public ResponseEntity<Medico> obtenerMedicoPorId(
            @Parameter(description = "ID del médico") @PathVariable Long id) {
        return medicoService.obtenerMedicoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza la información de un médico existente.
     *
     * @param medico el médico con los datos actualizados
     * @return el médico actualizado
     */
    @PutMapping
    @Operation(summary = "Actualizar médico", description = "Actualiza la información de un médico existente")
    @ApiResponse(responseCode = "200", description = "Médico actualizado exitosamente")
    public Medico actualizarMedico(@RequestBody Medico medico) {
        return medicoService.actualizarMedico(medico);
    }

    /**
     * Elimina un médico del sistema.
     *
     * @param id el ID del médico a eliminar
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar médico", description = "Elimina un médico del sistema")
    @ApiResponse(responseCode = "200", description = "Médico eliminado exitosamente")
    public void eliminarMedico(
            @Parameter(description = "ID del médico") @PathVariable Long id) {
        medicoService.eliminarMedico(id);
    }
}
