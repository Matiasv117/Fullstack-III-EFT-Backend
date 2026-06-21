package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.client.ListaEsperaClient;
import com.saludrednorte.ms_optimizacion.dto.ListaEsperaDTO;
import com.saludrednorte.ms_optimizacion.messaging.NotificacionEventPublisher;
import com.saludrednorte.ms_optimizacion.entity.Cita;
import com.saludrednorte.ms_optimizacion.entity.EstadoCita;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OptimizacionServiceTest {

    @Mock
    private OptimizacionFactory factory;

    @Mock
    private CitaService citaService;

    @Mock
    private ListaEsperaClient listaEsperaClient;

    @Mock
    private NotificacionEventPublisher notificacionEventPublisher;

    @Mock
    private EstrategiaOptimizacion estrategia;

    @InjectMocks
    private OptimizacionService optimizacionService;

    private Cita cita;
    private Medico medico;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        medico = new Medico();
        medico.setId(1L);
        medico.setNombre("Dr. Pedro");
        
        cita = new Cita();
        cita.setId(1L);
        cita.setPacienteId(100L);
        cita.setMedico(medico);
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setEstado(EstadoCita.CONFIRMADA);
    }

    @Test
    void testProcesarCancelacionExitosa() {
        // Given
        when(citaService.obtenerCitaPorId(1L)).thenReturn(Optional.of(cita));
        when(factory.getEstrategia("fifo")).thenReturn(estrategia);
        
        // When
        optimizacionService.procesarCancelacion(1L, "fifo");
        
        // Then
        verify(citaService, times(1)).cancelarCita(1L);
        verify(estrategia, times(1)).reasignarCita(cita);
        verify(notificacionEventPublisher, times(1)).publicar(eq(100L), eq("CITA_REASIGNADA"), anyString());
    }

    @Test
    void testProcesarCancelacionSinCita() {
        // Given
        when(citaService.obtenerCitaPorId(999L)).thenReturn(Optional.empty());
        
        // When
        optimizacionService.procesarCancelacion(999L, "fifo");
        
        // Then
        verify(citaService, times(1)).cancelarCita(999L);
        verify(estrategia, never()).reasignarCita(any());
        verify(notificationClient, never()).createNotification(any());
    }

    @Test
    void testObtenerListaEsperaExitosa() {
        // Given
        List<ListaEsperaDTO> lista = List.of(new ListaEsperaDTO());
        when(listaEsperaClient.getListaEspera()).thenReturn(lista);
        
        // When
        List<ListaEsperaDTO> resultado = optimizacionService.obtenerListaEspera();
        
        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(listaEsperaClient, times(1)).getListaEspera();
    }

    @Test
    void testFallbackListaEsperaEnCaso() {
        // Given
        Throwable excepcion = new RuntimeException("Servicio no disponible");
        
        // When
        List<ListaEsperaDTO> resultado = optimizacionService.fallbackListaEspera(excepcion);
        
        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testProcesarCancelacionConEstrategiaGravedad() {
        // Given
        when(citaService.obtenerCitaPorId(1L)).thenReturn(Optional.of(cita));
        when(factory.getEstrategia("gravedad")).thenReturn(estrategia);
        
        // When
        optimizacionService.procesarCancelacion(1L, "gravedad");
        
        // Then
        verify(citaService, times(1)).cancelarCita(1L);
        verify(factory, times(1)).getEstrategia("gravedad");
        verify(estrategia, times(1)).reasignarCita(cita);
    }

    @Test
    void testNotificacionFallidaNoAfectaFlujo() {
        // Given
        when(citaService.obtenerCitaPorId(1L)).thenReturn(Optional.of(cita));
        when(factory.getEstrategia("fifo")).thenReturn(estrategia);
        doThrow(new RuntimeException("Error en notificación"))
                .when(notificationClient).createNotification(any());
        
        // When - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            optimizacionService.procesarCancelacion(1L, "fifo");
        });
        
        // Then
        verify(citaService, times(1)).cancelarCita(1L);
        verify(estrategia, times(1)).reasignarCita(cita);
    }
}

