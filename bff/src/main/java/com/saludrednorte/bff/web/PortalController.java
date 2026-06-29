package com.saludrednorte.bff.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.saludrednorte.bff.service.PortalResumenService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador del portal de salud que expone endpoints agregados
 * para el dashboard del frontend, siguiendo el patrón BFF.
 */
@RestController
@RequestMapping(path = "/api/portal", produces = MediaType.APPLICATION_JSON_VALUE)
public class PortalController {

    private final PortalResumenService portalResumenService;

    public PortalController(PortalResumenService portalResumenService) {
        this.portalResumenService = portalResumenService;
    }

    /**
     * Vista agregada para el dashboard: pacientes + notificaciones pendientes.
     */
    @GetMapping("/resumen")
    public JsonNode resumen(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return portalResumenService.construirResumen(authorization);
    }
}
