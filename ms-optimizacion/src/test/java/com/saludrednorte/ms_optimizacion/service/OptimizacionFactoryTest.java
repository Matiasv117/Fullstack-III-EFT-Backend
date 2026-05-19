package com.saludrednorte.ms_optimizacion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class OptimizacionFactoryTest {

    @Mock
    private EstrategiaFIFO estrategiaFIFO;

    @Mock
    private EstrategiaPorGravedad estrategiaPorGravedad;

    @InjectMocks
    private OptimizacionFactory factory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testObtenerEstrategiaFIFO() {
        // Given & When
        EstrategiaOptimizacion resultado = factory.getEstrategia("fifo");
        
        // Then
        assertNotNull(resultado);
        assertEquals(estrategiaFIFO, resultado);
    }

    @Test
    void testObtenerEstrategiaGravedad() {
        // Given & When
        EstrategiaOptimizacion resultado = factory.getEstrategia("gravedad");
        
        // Then
        assertNotNull(resultado);
        assertEquals(estrategiaPorGravedad, resultado);
    }

    @Test
    void testObtenerEstrategiaDefaultFIFO() {
        // Given: tipo invalido
        // When
        EstrategiaOptimizacion resultado = factory.getEstrategia("desconocida");
        
        // Then: debe retornar FIFO por defecto
        assertNotNull(resultado);
        assertEquals(estrategiaFIFO, resultado);
    }

    @Test
    void testObtenerEstrategiaFIFOMayuscula() {
        // Given & When (case insensitive)
        EstrategiaOptimizacion resultado = factory.getEstrategia("FIFO");
        
        // Then
        assertNotNull(resultado);
        assertEquals(estrategiaFIFO, resultado);
    }

    @Test
    void testObtenerEstrategiaGravedadMayuscula() {
        // Given & When (case insensitive)
        EstrategiaOptimizacion resultado = factory.getEstrategia("GRAVEDAD");
        
        // Then
        assertNotNull(resultado);
        assertEquals(estrategiaPorGravedad, resultado);
    }
}

