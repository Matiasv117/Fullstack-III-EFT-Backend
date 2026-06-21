package com.saludrednorte.ms_listas_espera.service;

import com.saludrednorte.ms_listas_espera.dto.ListaEsperaMetricasDTO;
import com.saludrednorte.ms_listas_espera.repository.ListaEsperaProcedimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListaEsperaServiceMetricasTest {

    @Mock
    private ListaEsperaProcedimientoRepository procedimientoRepository;

    private ListaEsperaService listaEsperaService;

    @BeforeEach
    void setUp() {
        listaEsperaService = new ListaEsperaService();
        ReflectionTestUtils.setField(listaEsperaService, "procedimientoRepository", procedimientoRepository);
    }

    @Test
    void obtenerMetricas_debeRetornarDatosDelProcedimiento() {
        when(procedimientoRepository.obtenerMetricas())
                .thenReturn(new ListaEsperaMetricasDTO(5, 2, 1, 2));

        ListaEsperaMetricasDTO metricas = listaEsperaService.obtenerMetricas();

        assertThat(metricas.getTotalPendientes()).isEqualTo(5);
        assertThat(metricas.getPacientesGravedadAlta()).isEqualTo(2);
    }

    @Test
    void obtenerMetricas_sinPostgres_debeLanzarNotImplemented() {
        ListaEsperaService serviceSinPostgres = new ListaEsperaService();

        assertThatThrownBy(serviceSinPostgres::obtenerMetricas)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
                });
    }
}
