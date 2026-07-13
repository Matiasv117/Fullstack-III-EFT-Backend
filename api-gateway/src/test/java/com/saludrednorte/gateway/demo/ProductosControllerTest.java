package com.saludrednorte.gateway.demo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductosControllerTest {

    private final ProductosController controller = new ProductosController();

    @Test
    void listar() {
        assertThat(controller.listar()).isEqualTo("Productos protegidos");
    }
}
