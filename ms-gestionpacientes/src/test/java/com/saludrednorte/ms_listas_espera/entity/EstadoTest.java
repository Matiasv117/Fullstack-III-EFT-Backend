package com.saludrednorte.ms_listas_espera.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoTest {

    @Test
    void testEstadoPendiente() {
        assertThat(Estado.PENDIENTE).isNotNull();
        assertThat(Estado.PENDIENTE.name()).isEqualTo("PENDIENTE");
    }

    @Test
    void testEstadoAsignada() {
        assertThat(Estado.ASIGNADA).isNotNull();
        assertThat(Estado.ASIGNADA.name()).isEqualTo("ASIGNADA");
    }

    @Test
    void testEstadoFinalizada() {
        assertThat(Estado.FINALIZADA).isNotNull();
        assertThat(Estado.FINALIZADA.name()).isEqualTo("FINALIZADA");
    }

    @Test
    void testEstadoValueOf() {
        assertThat(Estado.valueOf("PENDIENTE")).isEqualTo(Estado.PENDIENTE);
        assertThat(Estado.valueOf("ASIGNADA")).isEqualTo(Estado.ASIGNADA);
        assertThat(Estado.valueOf("FINALIZADA")).isEqualTo(Estado.FINALIZADA);
    }

    @Test
    void testEstadoValues() {
        Estado[] valores = Estado.values();
        assertThat(valores).contains(Estado.PENDIENTE, Estado.ASIGNADA, Estado.FINALIZADA);
        assertThat(valores).hasSizeGreaterThanOrEqualTo(3);
    }
}


