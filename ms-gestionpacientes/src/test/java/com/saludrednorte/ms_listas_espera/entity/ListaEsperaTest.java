package com.saludrednorte.ms_listas_espera.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListaEsperaTest {

    @Test
    void testListaEsperaGettersSetters() {
        ListaEspera listaEspera = new ListaEspera();
        Paciente paciente = new Paciente();
        paciente.setId(1L);

        listaEspera.setId(5L);
        listaEspera.setPaciente(paciente);
        listaEspera.setInterconsulta("Cardiología");
        listaEspera.setGravedad(Gravedad.ALTA);
        listaEspera.setEstado(Estado.PENDIENTE);

        assertThat(listaEspera.getId()).isEqualTo(5L);
        assertThat(listaEspera.getPaciente()).isEqualTo(paciente);
        assertThat(listaEspera.getInterconsulta()).isEqualTo("Cardiología");
        assertThat(listaEspera.getGravedad()).isEqualTo(Gravedad.ALTA);
        assertThat(listaEspera.getEstado()).isEqualTo(Estado.PENDIENTE);
    }

    @Test
    void testListaEsperaValoresNulos() {
        ListaEspera listaEspera = new ListaEspera();

        assertThat(listaEspera.getId()).isNull();
        assertThat(listaEspera.getPaciente()).isNull();
        assertThat(listaEspera.getInterconsulta()).isNull();
        assertThat(listaEspera.getGravedad()).isNull();
        assertThat(listaEspera.getEstado()).isNull();
    }

    @Test
    void testListaEsperaGravedadBaja() {
        ListaEspera listaEspera = new ListaEspera();
        listaEspera.setGravedad(Gravedad.BAJA);

        assertThat(listaEspera.getGravedad()).isEqualTo(Gravedad.BAJA);
    }

    @Test
    void testListaEsperaGravedadMedia() {
        ListaEspera listaEspera = new ListaEspera();
        listaEspera.setGravedad(Gravedad.MEDIA);

        assertThat(listaEspera.getGravedad()).isEqualTo(Gravedad.MEDIA);
    }

    @Test
    void testListaEsperaEstadoAsignada() {
        ListaEspera listaEspera = new ListaEspera();
        listaEspera.setEstado(Estado.ASIGNADA);

        assertThat(listaEspera.getEstado()).isEqualTo(Estado.ASIGNADA);
    }

    @Test
    void testListaEsperaEstadoFinalizada() {
        ListaEspera listaEspera = new ListaEspera();
        listaEspera.setEstado(Estado.FINALIZADA);

        assertThat(listaEspera.getEstado()).isEqualTo(Estado.FINALIZADA);
    }
}

