package com.saludrednorte.ms_optimizacion.client;

import com.saludrednorte.ms_optimizacion.dto.PacienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-listas-espera", contextId = "pacienteClient", url = "${MS_LISTAS_ESPERA_URL:http://localhost:8083}")
public interface PacienteClient {

    @GetMapping("/pacientes/{id}")
    PacienteDTO obtenerPacientePorId(@PathVariable("id") Long id);
}
