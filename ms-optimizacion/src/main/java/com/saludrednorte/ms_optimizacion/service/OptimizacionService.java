package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.dto.ListaEsperaDTO;
import com.saludrednorte.ms_optimizacion.dto.NotificationRequestDTO;
import com.saludrednorte.ms_optimizacion.client.ListaEsperaClient;
import com.saludrednorte.ms_optimizacion.client.NotificationClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de optimizacion: coordina reasignacion de citas y calculo de prioridad.
 */
@Service
public class OptimizacionService {

    private static final Logger logger = LoggerFactory.getLogger(OptimizacionService.class);

    @Autowired
    private CitaService citaService;

    @Autowired
    private ListaEsperaClient listaEsperaClient;

    @Autowired
    private NotificationClient notificationClient;

    public void procesarCancelacion(Long citaId, String estrategiaTipo) {
        // Placeholder: cancelar cita según estrategia
        citaService.cancelarCita(citaId);
        logger.info("Cita {} cancelada con estrategia {}", citaId, estrategiaTipo);
    }

    @CircuitBreaker(name = "listaEsperaService", fallbackMethod = "fallbackListaEspera")
    public List<ListaEsperaDTO> obtenerListaEspera() {
        // Llamada a ms-gestionpacientes usando Feign
        return listaEsperaClient.getListaEspera();
    }

    public List<ListaEsperaDTO> fallbackListaEspera(Throwable t) {
        // Retornar lista vacía o datos locales
        logger.warn("Fallback activado para obtenerListaEspera");
        return List.of();
    }

    /**
     * Calcula la prioridad de un paciente segun gravedad, distancia y dias de espera.
     *
     * @param gravedad nivel 1-5
     * @param distanciaKm distancia geografica en kilometros
     * @param diasEspera dias acumulados en espera
     * @return nivel de prioridad calculado
     */
    public NivelPrioridad calcularPrioridadPaciente(int gravedad, double distanciaKm, int diasEspera) {
        // Placeholder: calcular prioridad basado en parámetros
        if (gravedad >= 4) {
            return NivelPrioridad.ALTA;
        } else if (gravedad >= 3 || diasEspera > 30) {
            return NivelPrioridad.MEDIA;
        } else {
            return NivelPrioridad.BAJA;
        }
    }
}
