package com.saludrednorte.ms_listas_espera.controller;

import com.saludrednorte.ms_listas_espera.client.CitaClient;
import com.saludrednorte.ms_listas_espera.dto.CitaDTO;
import com.saludrednorte.ms_listas_espera.entity.ListaEspera;
import com.saludrednorte.ms_listas_espera.entity.Paciente;
import com.saludrednorte.ms_listas_espera.service.ListaEsperaService;
import com.saludrednorte.ms_listas_espera.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para el portal de pacientes.
 * <p>
 * Proporciona endpoints específicos para que los pacientes puedan ver
 * su información personal, su posición en la lista de espera y sus citas.
 * Todos los endpoints requieren autenticación con token de paciente.
 * </p>
 */
@RestController
@RequestMapping("/pacientes/portal")
@Tag(name = "Portal Pacientes", description = "API para que los pacientes consulten su información personal")
public class PacientePortalController {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private ListaEsperaService listaEsperaService;

    @Autowired
    private CitaClient citaClient;

    /**
     * Obtiene los datos personales del paciente autenticado.
     * El paciente ID se extrae del token JWT (formato: PACIENTE_{id}).
     *
     * @param authorization token JWT del paciente
     * @return datos personales del paciente
     */
    @GetMapping("/mis-datos")
    @Operation(summary = "Mis datos personales", description = "Obtiene la información personal del paciente autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    public ResponseEntity<?> getMisDatos(
            @Parameter(description = "Token JWT del paciente") @RequestHeader("Authorization") String authorization) {
        try {
            Long pacienteId = extractPacienteIdFromToken(authorization);
            if (pacienteId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token inválido o no es de paciente"));
            }

            Optional<Paciente> paciente = pacienteService.obtenerPacientePorId(pacienteId);
            return paciente.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error al procesar token"));
        }
    }

    /**
     * Obtiene la posición del paciente en la lista de espera.
     *
     * @param authorization token JWT del paciente
     * @return posición y detalles en la lista de espera
     */
    @GetMapping("/mi-posicion")
    @Operation(summary = "Mi posición en lista de espera", description = "Obtiene la posición actual del paciente en la lista de espera")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posición obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Paciente no está en lista de espera")
    })
    public ResponseEntity<?> getMiPosicion(
            @Parameter(description = "Token JWT del paciente") @RequestHeader("Authorization") String authorization) {
        try {
            Long pacienteId = extractPacienteIdFromToken(authorization);
            if (pacienteId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token inválido o no es de paciente"));
            }

            List<ListaEspera> listaEspera = listaEsperaService.obtenerListaEspera();
            Optional<ListaEspera> miRegistro = listaEspera.stream()
                    .filter(le -> le.getPaciente() != null && le.getPaciente().getId().equals(pacienteId))
                    .findFirst();

            if (miRegistro.isPresent()) {
                int posicion = (int) listaEspera.stream()
                        .filter(le -> le.getId().compareTo(miRegistro.get().getId()) <= 0)
                        .count();

                return ResponseEntity.ok(Map.of(
                        "posicion", posicion,
                        "total", listaEspera.size(),
                        "registro", miRegistro.get()
                ));
            } else {
                Map<String, Object> resp = new HashMap<>();
                resp.put("posicion", null);
                resp.put("total", listaEspera.size());
                return ResponseEntity.ok(resp);
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error al procesar token"));
        }
    }

    /**
     * Obtiene las citas programadas para el paciente autenticado.
     *
     * @param authorization token JWT del paciente
     * @return lista de citas del paciente con estado CONFIRMADA
     */
    @GetMapping("/mis-citas")
    @Operation(summary = "Mis citas", description = "Obtiene las citas programadas para el paciente autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Citas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<?> getMisCitas(
            @Parameter(description = "Token JWT del paciente") @RequestHeader("Authorization") String authorization) {
        try {
            Long pacienteId = extractPacienteIdFromToken(authorization);
            if (pacienteId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token inválido o no es de paciente"));
            }

            List<CitaDTO> citas = citaClient.obtenerCitasPorPaciente(pacienteId);
            return ResponseEntity.ok(citas);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Error al obtener citas"));
        }
    }

    /**
     * Extrae el ID del paciente desde el token JWT.
     * El token tiene el formato: PACIENTE_{id}
     *
     * @param authorization header Authorization
     * @return ID del paciente o null si no es válido
     */
    private Long extractPacienteIdFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        
        String token = authorization.substring(7);
        // El username en el token tiene el formato PACIENTE_{id}
        // Por simplicidad, extraemos el ID del username
        // En una implementación real, se debería validar el token JWT
        
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            // Decodificar el payload (base64)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            // Extraer el subject (sub) del payload
            String sub = payload.substring(payload.indexOf("\"sub\":\"") + 7);
            sub = sub.substring(0, sub.indexOf("\""));
            
            if (sub.startsWith("PACIENTE_")) {
                return Long.parseLong(sub.substring(9));
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
