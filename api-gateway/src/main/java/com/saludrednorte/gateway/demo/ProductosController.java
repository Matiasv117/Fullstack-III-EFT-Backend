package com.saludrednorte.gateway.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de demostración para verificar la protección de rutas del API Gateway.
 */
@RestController
public class ProductosController {

    /**
     * Endpoint de ejemplo protegido por el API Gateway.
     *
     * @return mensaje de confirmación de acceso
     */
    @GetMapping("/productos")
    public String listar() {
        return "Productos protegidos";
    }
}

