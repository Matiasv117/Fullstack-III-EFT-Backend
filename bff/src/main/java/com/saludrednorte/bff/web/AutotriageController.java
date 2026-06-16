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

@RestController
@RequestMapping(path = "/api/autotriage", produces = MediaType.APPLICATION_JSON_VALUE)
public class AutotriageController {

    private final AutotriageService autotriageService;

    public AutotriageController(AutotriageService autotriageService) {
        this.autotriageService = autotriageService;
    }

    @PostMapping
    public ResponseEntity<JsonNode> recibirAutotriage(@RequestBody AutotriageRequest request,
                                                     @RequestHeader(value = "Authorization", required = false) String auth) {
        JsonNode resultado = autotriageService.procesar(request, auth);
        return ResponseEntity.ok(resultado);
    }
}
