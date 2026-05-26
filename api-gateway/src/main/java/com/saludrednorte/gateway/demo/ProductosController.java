package com.saludrednorte.gateway.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductosController {

    @GetMapping("/productos")
    public String listar() {
        return "Productos protegidos";
    }
}

