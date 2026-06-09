package com.saludrednorte.ms_optimizacion.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrioridadCalculadoraTest {

    private final PrioridadCalculadora calculadora = new PrioridadCalculadora();

    @Test
    void calcularNivel_retornaAlta_conGravedadMaximaYEsperaAlta() {
        NivelPrioridad nivel = calculadora.calcularNivel(5, 10.0, 60);

        assertThat(nivel).isEqualTo(NivelPrioridad.ALTA);
    }

    @Test
    void calcularNivel_retornaMedia_conValoresIntermedios() {
        NivelPrioridad nivel = calculadora.calcularNivel(3, 40.0, 20);

        assertThat(nivel).isEqualTo(NivelPrioridad.MEDIA);
    }

    @Test
    void calcularNivel_retornaBaja_conValoresBajos() {
        NivelPrioridad nivel = calculadora.calcularNivel(1, 0.0, 0);

        assertThat(nivel).isEqualTo(NivelPrioridad.BAJA);
    }

    @Test
    void calcularNivel_falla_conGravedadFueraDeRango() {
        assertThatThrownBy(() -> calculadora.calcularNivel(0, 10.0, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gravedad");
    }

    @Test
    void calcularNivel_falla_conDistanciaNegativa() {
        assertThatThrownBy(() -> calculadora.calcularNivel(3, -1.0, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("distancia");
    }

    @Test
    void calcularNivel_falla_conEsperaNegativa() {
        assertThatThrownBy(() -> calculadora.calcularNivel(3, 1.0, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("espera");
    }
}

