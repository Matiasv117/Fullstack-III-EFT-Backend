package com.saludrednorte.ms_auth.client;

import com.saludrednorte.ms_auth.dto.PacienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Cliente Feign para comunicarse con ms-gestionpacientes.
 * 
 * NOTA: ms-gestionpacientes usa el campo "dni" internamente, pero para Chile
 * esto corresponde al RUT. El parámetro se llama "dni" para mantener compatibilidad
 * con el API existente de ms-gestionpacientes.
 */
@FeignClient(name = "ms-gestionpacientes", url = "${ms-gestionpacientes.url:http://localhost:8083}")
public interface PacienteClient {

    @GetMapping("/pacientes/buscar")
    PacienteDTO buscarPaciente(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("dni") String rut  // Parámetro enviado como "dni" pero contiene el RUT
    );

    @PostMapping("/pacientes")
    PacienteDTO crearPaciente(@RequestBody PacienteDTO paciente);
}
