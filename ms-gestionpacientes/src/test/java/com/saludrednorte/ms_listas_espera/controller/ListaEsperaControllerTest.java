package com.saludrednorte.ms_listas_espera.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saludrednorte.ms_listas_espera.dto.ListaEsperaMetricasDTO;
import com.saludrednorte.ms_listas_espera.entity.Estado;
import com.saludrednorte.ms_listas_espera.entity.Gravedad;
import com.saludrednorte.ms_listas_espera.entity.ListaEspera;
import com.saludrednorte.ms_listas_espera.entity.Paciente;
import com.saludrednorte.ms_listas_espera.service.ListaEsperaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ListaEsperaControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ListaEsperaService listaEsperaService;

    @InjectMocks
    private ListaEsperaController listaEsperaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(listaEsperaController).build();
    }

    @Test
    void agregarAListaEspera_debeRetornarRegistro() throws Exception {
        ListaEspera entrada = new ListaEspera();
        Paciente paciente = new Paciente();
        paciente.setId(1L);
        entrada.setPaciente(paciente);
        ListaEspera guardado = new ListaEspera();
        guardado.setId(10L);
        when(listaEsperaService.agregarAListaEspera(any(ListaEspera.class))).thenReturn(guardado);

        mockMvc.perform(post("/lista-espera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void obtenerListaEspera_debeRetornarLista() throws Exception {
        ListaEspera item = new ListaEspera();
        item.setId(1L);
        when(listaEsperaService.obtenerListaEspera()).thenReturn(List.of(item));

        mockMvc.perform(get("/lista-espera"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerPorEstado_debeRetornarListaFiltrada() throws Exception {
        when(listaEsperaService.obtenerPorEstado(Estado.PENDIENTE)).thenReturn(List.of());

        mockMvc.perform(get("/lista-espera/estado/PENDIENTE"))
                .andExpect(status().isOk());

        verify(listaEsperaService).obtenerPorEstado(Estado.PENDIENTE);
    }

    @Test
    void obtenerPorGravedad_debeRetornarListaFiltrada() throws Exception {
        when(listaEsperaService.obtenerPorGravedad(Gravedad.ALTA)).thenReturn(List.of());

        mockMvc.perform(get("/lista-espera/gravedad/ALTA"))
                .andExpect(status().isOk());

        verify(listaEsperaService).obtenerPorGravedad(Gravedad.ALTA);
    }

    @Test
    void obtenerMetricas_debeRetornarMetricas() throws Exception {
        ListaEsperaMetricasDTO metricas = new ListaEsperaMetricasDTO(5L, 2L, 2L, 1L);
        when(listaEsperaService.obtenerMetricas()).thenReturn(metricas);

        mockMvc.perform(get("/lista-espera/metricas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPendientes").value(5));
    }

    @Test
    void obtenerPorId_debeRetornar200CuandoExiste() throws Exception {
        ListaEspera item = new ListaEspera();
        item.setId(4L);
        when(listaEsperaService.obtenerPorId(4L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/lista-espera/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void obtenerPorId_debeRetornar404CuandoNoExiste() throws Exception {
        when(listaEsperaService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/lista-espera/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarEstado_debeRetornarRegistroActualizado() throws Exception {
        ListaEspera item = new ListaEspera();
        item.setId(2L);
        when(listaEsperaService.actualizarEstado(2L, Estado.ASIGNADA)).thenReturn(item);

        mockMvc.perform(put("/lista-espera/2/estado/ASIGNADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void eliminarDeListaEspera_debeInvocarServicio() throws Exception {
        mockMvc.perform(delete("/lista-espera/8"))
                .andExpect(status().isOk());

        verify(listaEsperaService).eliminarDeListaEspera(8L);
    }
}
