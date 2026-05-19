package com.saludrednorte.ms_listas_espera.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GravedadTest {

    @Test
    void testGravedadBaja() {
        assertThat(Gravedad.BAJA).isNotNull();
        assertThat(Gravedad.BAJA.name()).isEqualTo("BAJA");
    }

    @Test
    void testGravedadMedia() {
        assertThat(Gravedad.MEDIA).isNotNull();
        assertThat(Gravedad.MEDIA.name()).isEqualTo("MEDIA");
    }

    @Test
    void testGravedadAlta() {
        assertThat(Gravedad.ALTA).isNotNull();
        assertThat(Gravedad.ALTA.name()).isEqualTo("ALTA");
    }

    @Test
    void testGravedadValueOf() {
        assertThat(Gravedad.valueOf("BAJA")).isEqualTo(Gravedad.BAJA);
        assertThat(Gravedad.valueOf("MEDIA")).isEqualTo(Gravedad.MEDIA);
        assertThat(Gravedad.valueOf("ALTA")).isEqualTo(Gravedad.ALTA);
    }

    @Test
    void testGravedadValues() {
        Gravedad[] valores = Gravedad.values();
        assertThat(valores).contains(Gravedad.BAJA, Gravedad.MEDIA, Gravedad.ALTA);
        assertThat(valores).hasSizeGreaterThanOrEqualTo(3);
    }
}


