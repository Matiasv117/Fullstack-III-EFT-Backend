package com.saludrednorte.bff.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.saludrednorte.bff.service.AutotriageService;
import com.saludrednorte.bff.web.dto.AutotriageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para el proceso de autotriage de pacientes.
 * Recibe solicitudes de clasificación de urgencia y las delega
 * a los microservicios de optimización y lista de espera.
 */
@Tag(name = "Autotriage", description = "Operaciones de autotriage")
@RestController
@RequestMapping(path = "/api/autotriage", produces = MediaType.APPLICATION_JSON_VALUE)
public class AutotriageController {

    private final AutotriageService autotriageService;

    public AutotriageController(AutotriageService autotriageService) {
        this.autotriageService = autotriageService;
    }

    /**
     * Recibe una solicitud de autotriage, calcula la prioridad y registra
     * al paciente en la lista de espera.
     *
     * @param request datos del paciente y gravedad
     * @param auth    token JWT opcional para autenticación downstream
     * @return resultado del autotriage con prioridad y confirmación
     */
    @PostMapping
    public ResponseEntity<JsonNode> recibirAutotriage(@RequestBody AutotriageRequest request,
                                                      @RequestHeader(value = "Authorization", required = false) String auth) {
        JsonNode resultado = autotriageService.procesar(request, auth);
        return ResponseEntity.ok(resultado);
    }
}
