package com.saludrednorte.ms_auth.client;

import com.saludrednorte.ms_auth.dto.PacienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Cliente Feign para comunicarse con ms-gestionpacientes.
 */
@FeignClient(name = "ms-gestionpacientes", url = "${ms-gestionpacientes.url:http://localhost:8083}")
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
