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

@Component
public class EstrategiaLIFO implements EstrategiaOptimizacion {

    private static final Logger logger = LoggerFactory.getLogger(EstrategiaLIFO.class);

    @Autowired
    private ListaEsperaClient listaEsperaClient;

    @Autowired
    private CitaService citaService;

    @Override
    public void reasignarCita(Cita citaCancelada) {
        List<ListaEsperaDTO> listaEspera = listaEsperaClient.getListaEspera();
        if (listaEspera == null || listaEspera.isEmpty()) {
            logger.info("No hay pacientes en lista de espera para reasignar la cita {}", citaCancelada.getId());
            return;
        }

        ListaEsperaDTO candidato = listaEspera.stream()
                .max(Comparator.comparing(ListaEsperaDTO::getId))
                .orElse(null);

        if (candidato == null) {
            logger.info("No se encontro un candidato para reasignar la cita {}", citaCancelada.getId());
            return;
        }

        citaCancelada.setPacienteId(candidato.getPacienteId());
        citaService.actualizarCita(citaCancelada);
        listaEsperaClient.actualizarEstado(candidato.getId(), "ASIGNADA");
        logger.info("Cita {} reasignada al paciente {} por LIFO", citaCancelada.getId(), candidato.getPacienteId());
    }
}
