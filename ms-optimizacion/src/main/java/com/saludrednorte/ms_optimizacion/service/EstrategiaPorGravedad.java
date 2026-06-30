package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.client.ListaEsperaClient;
import com.saludrednorte.ms_optimizacion.dto.ListaEsperaDTO;
import com.saludrednorte.ms_optimizacion.entity.Cita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Estrategia por gravedad: reasigna la cita al paciente con mayor prioridad calculada.
 */
@Component
public class EstrategiaPorGravedad implements EstrategiaOptimizacion {

    private static final Logger logger = LoggerFactory.getLogger(EstrategiaPorGravedad.class);

    @Autowired
    private ListaEsperaClient listaEsperaClient;

    @Autowired
    private PrioridadCalculadora prioridadCalculadora;

    @Autowired
    private CitaService citaService;

    /**
     * Reasigna la cita al paciente con mayor nivel de prioridad en lista de espera.
     *
     * @param citaCancelada cita cancelada a reasignar
     */
    @Override
    public void reasignarCita(Cita citaCancelada) {
        List<ListaEsperaDTO> listaEspera = listaEsperaClient.getListaEspera();
        if (listaEspera == null || listaEspera.isEmpty()) {
            logger.info("No hay pacientes en lista de espera para reasignar la cita {}", citaCancelada.getId());
            return;
        }

        ListaEsperaDTO candidato = listaEspera.stream()
                .max(Comparator.comparing(this::calcularNivel))
                .orElse(null);

        if (candidato == null) {
            logger.info("No se encontro un candidato para reasignar la cita {}", citaCancelada.getId());
            return;
        }

        citaCancelada.setPacienteId(candidato.getPacienteId());
        citaService.actualizarCita(citaCancelada);
        listaEsperaClient.actualizarEstado(candidato.getId(), "ASIGNADA");
        logger.info("Cita {} reasignada al paciente {} por prioridad", citaCancelada.getId(), candidato.getPacienteId());
    }

    private NivelPrioridad calcularNivel(ListaEsperaDTO registro) {
        int gravedad = mapearGravedad(registro.getGravedad());
        int diasEspera = mapearEspera(registro.getGravedad());
        return prioridadCalculadora.calcularNivel(gravedad, 0.0, diasEspera);
    }

    private int mapearGravedad(String gravedad) {
        if (gravedad == null) {
            return 1;
        }
        switch (gravedad.toUpperCase()) {
            case "ALTA":
                return 5;
            case "MEDIA":
                return 3;
            default:
                return 1;
        }
    }

    private int mapearEspera(String gravedad) {
        if (gravedad == null) {
            return 0;
        }
        switch (gravedad.toUpperCase()) {
            case "ALTA":
                return 60;
            case "MEDIA":
                return 30;
            default:
                return 0;
        }
    }
}
