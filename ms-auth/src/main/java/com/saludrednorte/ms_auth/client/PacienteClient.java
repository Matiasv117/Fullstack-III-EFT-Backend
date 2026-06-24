package com.saludrednorte.ms_auth.client;

import com.saludrednorte.ms_auth.dto.PacienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Cliente Feign para comunicarse con el microservicio de pacientes.
 * Se resuelve vía Eureka con el nombre de aplicación real: ms-listas-espera.
 */
@FeignClient(name = "ms-listas-espera")
public interface PacienteClient {

    @GetMapping("/pacientes/buscar")
    PacienteDTO buscarPaciente(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("dni") String dni
    );

    @PostMapping("/pacientes")
    PacienteDTO crearPaciente(@RequestBody PacienteDTO paciente);
}
