package com.saludrednorte.ms_auth.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RutUtilTest {

    @Test
    void limpiarRut_remuevePuntosYGuiones() {
        assertThat(RutUtil.limpiarRut("12.345.678-9")).isEqualTo("123456789");
    }

    @Test
    void limpiarRut_remueveEspacios() {
        assertThat(RutUtil.limpiarRut("12.345.678-9")).isEqualTo("123456789");
    }

    @Test
    void limpiarRut_retornaNullSiEsNull() {
        assertThat(RutUtil.limpiarRut(null)).isNull();
    }

    @Test
    void limpiarRut_sinFormato() {
        assertThat(RutUtil.limpiarRut("123456789")).isEqualTo("123456789");
    }

    @Test
    void formatearRut_formatoEstandar() {
        assertThat(RutUtil.formatearRut("123456789")).isEqualTo("12.345.678-9");
    }

    @Test
    void formatearRut_conDigitoK() {
        assertThat(RutUtil.formatearRut("12345678k")).isEqualTo("12.345.678-K");
    }

    @Test
    void formatearRut_retornaNullSiEsNull() {
        assertThat(RutUtil.formatearRut(null)).isNull();
    }

    @Test
    void formatearRut_retornaVacioSiEsVacio() {
        assertThat(RutUtil.formatearRut("")).isEmpty();
    }

    @Test
    void formatearRut_cadenaCorta() {
        assertThat(RutUtil.formatearRut("5")).isEqualTo("5");
    }

    @Test
    void formatearRut_desdeFormateado() {
        assertThat(RutUtil.formatearRut("12.345.678-9")).isEqualTo("12.345.678-9");
    }

    @Test
    void validarFormato_rutValido() {
        assertThat(RutUtil.validarFormato("12.345.678-5")).isTrue();
    }

    @Test
    void validarFormato_rutInvalido() {
        assertThat(RutUtil.validarFormato("12.345.678-0")).isFalse();
    }

    @Test
    void validarFormato_null() {
        assertThat(RutUtil.validarFormato(null)).isFalse();
    }

    @Test
    void validarFormato_vacio() {
        assertThat(RutUtil.validarFormato("")).isFalse();
    }

    @Test
    void validarFormato_rutInvalidoPorDigitoVerificador() {
        assertThat(RutUtil.validarFormato("11.111.111-0")).isFalse();
    }

    @Test
    void validarFormato_conDigitoK() {
        assertThat(RutUtil.validarFormato("1.234.567-8")).isFalse();
    }

    @Test
    void validarFormato_rutCorto() {
        assertThat(RutUtil.validarFormato("1-0")).isFalse();
    }

    @Test
    void normalizeRut_valido() {
        assertThat(RutUtil.normalizarRut("12345678-5")).isEqualTo("12.345.678-5");
    }

    @Test
    void normalizeRut_invalidoLanzaExcepcion() {
        assertThatThrownBy(() -> RutUtil.normalizarRut("12.345.678-0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RUT inválido");
    }

    @Test
    void normalizeRut_nullLanzaExcepcion() {
        assertThatThrownBy(() -> RutUtil.normalizarRut(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
