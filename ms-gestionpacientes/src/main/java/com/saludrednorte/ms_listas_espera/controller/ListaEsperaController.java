package com.saludrednorte.ms_listas_espera.controller;

import com.saludrednorte.ms_listas_espera.dto.ListaEsperaMetricasDTO;
import com.saludrednorte.ms_listas_espera.entity.Estado;
import com.saludrednorte.ms_listas_espera.entity.Gravedad;
import com.saludrednorte.ms_listas_espera.entity.ListaEspera;
import com.saludrednorte.ms_listas_espera.service.ListaEsperaService;
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
 * Controlador REST para la gestión de la lista de espera de pacientes.
 * <p>
 * Proporciona endpoints para agregar, consultar, actualizar y eliminar pacientes
 * de la lista de espera, con filtros por estado y gravedad.
 * </p>
 */
@RestController
@RequestMapping("/lista-espera")
@Tag(name = "Lista de Espera", description = "API para la gestión de la lista de espera de pacientes")
public class ListaEsperaController {

    @Autowired
    private ListaEsperaService listaEsperaService;

    /**
     * Agrega un paciente a la lista de espera.
     *
     * @param listaEspera el registro de lista de espera a agregar
     * @return el registro agregado con ID asignado
     */
    @PostMapping
    @Operation(summary = "Agregar paciente a lista de espera", description = "Agrega un paciente a la lista de espera del sistema")
    @ApiResponse(responseCode = "200", description = "Paciente agregado a lista de espera exitosamente")
    public ListaEspera agregarAListaEspera(@RequestBody ListaEspera listaEspera) {
        return listaEsperaService.agregarAListaEspera(listaEspera);
    }

    /**
     * Obtiene la lista completa de espera.
     *
     * @return lista de todos los registros de lista de espera
     */
    @GetMapping
    @Operation(summary = "Obtener lista de espera", description = "Retorna la lista completa de espera")
    @ApiResponse(responseCode = "200", description = "Lista de espera obtenida exitosamente")
    public List<ListaEspera> obtenerListaEspera() {
        return listaEsperaService.obtenerListaEspera();
    }

    /**
     * Obtiene pacientes en lista de espera filtrados por estado.
     *
     * @param estado el estado a filtrar
     * @return lista de registros con el estado especificado
     */
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener lista de espera por estado", description = "Retorna pacientes en lista de espera filtrados por estado")
    @ApiResponse(responseCode = "200", description = "Lista filtrada obtenida exitosamente")
    public List<ListaEspera> obtenerPorEstado(
            @Parameter(description = "Estado del paciente") @PathVariable Estado estado) {
        return listaEsperaService.obtenerPorEstado(estado);
    }

    /**
     * Obtiene pacientes en lista de espera filtrados por gravedad.
     *
     * @param gravedad el nivel de gravedad a filtrar
     * @return lista de registros con la gravedad especificada
     */
    @GetMapping("/gravedad/{gravedad}")
    @Operation(summary = "Obtener lista de espera por gravedad", description = "Retorna pacientes en lista de espera filtrados por nivel de gravedad")
    @ApiResponse(responseCode = "200", description = "Lista filtrada obtenida exitosamente")
    public List<ListaEspera> obtenerPorGravedad(
            @Parameter(description = "Nivel de gravedad") @PathVariable Gravedad gravedad) {
        return listaEsperaService.obtenerPorGravedad(gravedad);
    }

    /**
     * Obtiene métricas de la lista de espera calculadas vía stored procedure.
     *
     * @return métricas como total de pacientes, distribución por gravedad, etc.
     */
    @GetMapping("/metricas")
    @Operation(summary = "Métricas de lista de espera", description = "Retorna métricas calculadas vía stored procedure PostgreSQL")
    public ListaEsperaMetricasDTO obtenerMetricas() {
        return listaEsperaService.obtenerMetricas();
    }

    /**
     * Obtiene un registro de lista de espera por su ID.
     *
     * @param id el ID del registro
     * @return ResponseEntity con el registro si existe, 404 si no existe
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener paciente de lista de espera por ID", description = "Retorna un paciente específico de la lista de espera")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    public ResponseEntity<ListaEspera> obtenerPorId(
            @Parameter(description = "ID del paciente en lista de espera") @PathVariable Long id) {
        return listaEsperaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza el estado de un paciente en la lista de espera.
     *
     * @param id el ID del paciente
     * @param estado el nuevo estado
     * @return el registro actualizado
     */
    @PutMapping("/{id}/estado/{estado}")
    @Operation(summary = "Actualizar estado de paciente en lista de espera", description = "Actualiza el estado de un paciente en la lista de espera")
    @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente")
    public ListaEspera actualizarEstado(
            @Parameter(description = "ID del paciente") @PathVariable Long id,
            @Parameter(description = "Nuevo estado") @PathVariable Estado estado) {
        return listaEsperaService.actualizarEstado(id, estado);
    }

    /**
     * Elimina un paciente de la lista de espera.
     *
     * @param id el ID del paciente a eliminar
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar paciente de lista de espera", description = "Elimina un paciente de la lista de espera")
    @ApiResponse(responseCode = "200", description = "Paciente eliminado de lista de espera exitosamente")
    public void eliminarDeListaEspera(
            @Parameter(description = "ID del paciente") @PathVariable Long id) {
        listaEsperaService.eliminarDeListaEspera(id);
    }
}
