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
 * Estrategia FIFO: reasigna la cita al paciente con menor ID en lista de espera.
 */
@Component
public class EstrategiaFIFO implements EstrategiaOptimizacion {

    private static final Logger logger = LoggerFactory.getLogger(EstrategiaFIFO.class);

    @Autowired
    private ListaEsperaClient listaEsperaClient;

    @Autowired
    private CitaService citaService;

    /**
     * Reasigna la cita al primer paciente disponible en lista de espera (FIFO).
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
                .min(Comparator.comparing(ListaEsperaDTO::getId))
                .orElse(null);

        if (candidato == null) {
            logger.info("No se encontro un candidato para reasignar la cita {}", citaCancelada.getId());
            return;
        }

        citaCancelada.setPacienteId(candidato.getPacienteId());
        citaService.actualizarCita(citaCancelada);
        logger.info("Cita {} reasignada al paciente {} por FIFO", citaCancelada.getId(), candidato.getPacienteId());
    }
}
