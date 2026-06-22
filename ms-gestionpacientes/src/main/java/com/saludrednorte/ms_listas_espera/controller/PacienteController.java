package com.saludrednorte.ms_listas_espera.controller;

import com.saludrednorte.ms_listas_espera.entity.Paciente;
import com.saludrednorte.ms_listas_espera.service.PacienteService;
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
 * Controlador REST para la gestión de pacientes.
 * <p>
 * Proporciona endpoints para registrar, consultar, actualizar y eliminar pacientes.
 * El registro automático de pacientes genera citas y notificaciones.
 * </p>
 */
@RestController
@RequestMapping("/pacientes")
@Tag(name = "Pacientes", description = "API para la gestión de pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    /**
     * Registra un nuevo paciente en el sistema.
     *
     * @param paciente el paciente a registrar
     * @return el paciente registrado con ID asignado
     */
    @PostMapping
    @Operation(summary = "Registrar un nuevo paciente", description = "Crea un nuevo paciente en el sistema y genera automáticamente una cita y notificación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente registrado exitosamente"),
            @ApiResponse(responseCode = "409", description = "Ya existe un paciente con el DNI indicado")
    })
    public Paciente registrarPaciente(@RequestBody Paciente paciente) {
        return pacienteService.registrarPaciente(paciente);
    }

    /**
     * Obtiene todos los pacientes registrados.
     *
     * @return lista de todos los pacientes
     */
    @GetMapping
    @Operation(summary = "Obtener todos los pacientes", description = "Retorna una lista con todos los pacientes registrados")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes obtenida exitosamente")
    public List<Paciente> obtenerTodosPacientes() {
        return pacienteService.obtenerTodosPacientes();
    }

    /**
     * Obtiene un paciente por su ID.
     *
     * @param id el ID del paciente
     * @return ResponseEntity con el paciente si existe, 404 si no existe
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener paciente por ID", description = "Retorna un paciente específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    public ResponseEntity<Paciente> obtenerPacientePorId(
            @Parameter(description = "ID del paciente") @PathVariable Long id) {
        return pacienteService.obtenerPacientePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza la información de un paciente existente.
     *
     * @param paciente el paciente con los datos actualizados
     * @return el paciente actualizado
     */
    @PutMapping
    @Operation(summary = "Actualizar paciente", description = "Actualiza la información de un paciente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    public Paciente actualizarPaciente(@RequestBody Paciente paciente) {
        return pacienteService.actualizarPaciente(paciente);
    }

    /**
     * Elimina un paciente del sistema.
     *
     * @param id el ID del paciente a eliminar
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar paciente", description = "Elimina un paciente del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    public void eliminarPaciente(
            @Parameter(description = "ID del paciente") @PathVariable Long id) {
        pacienteService.eliminarPaciente(id);
    }

    /**
     * Busca un paciente por nombre, apellido y RUT.
     *
     * @param nombre el nombre del paciente
     * @param apellido el apellido del paciente
     * @param dni el RUT/DNI del paciente
     * @return ResponseEntity con el paciente si existe, 404 si no existe
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar paciente por datos personales", description = "Busca un paciente por nombre, apellido y RUT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    public ResponseEntity<Paciente> buscarPaciente(
            @Parameter(description = "Nombre del paciente") @RequestParam String nombre,
            @Parameter(description = "Apellido del paciente") @RequestParam String apellido,
            @Parameter(description = "RUT/DNI del paciente") @RequestParam String dni) {
        return pacienteService.buscarPorNombreApellidoDni(nombre, apellido, dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
