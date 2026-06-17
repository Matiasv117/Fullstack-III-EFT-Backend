package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.client.ListaEsperaClient;
import com.saludrednorte.ms_optimizacion.client.NotificationClient;
import com.saludrednorte.ms_optimizacion.dto.ListaEsperaDTO;
import com.saludrednorte.ms_optimizacion.dto.NotificationRequestDTO;
import com.saludrednorte.ms_optimizacion.entity.Cita;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de optimización: coordina reasignación de citas y cálculo de prioridad.
 * <p>
 * Este servicio gestiona la reasignación automática de citas cuando ocurren cancelaciones,
 * utilizando diferentes estrategias de optimización (FIFO, por gravedad).
 * También proporciona cálculo de prioridad para pacientes en lista de espera.
 * </p>
 */
@Service
public class OptimizacionService {

    private static final Logger logger = LoggerFactory.getLogger(OptimizacionService.class);

    @Autowired
    private OptimizacionFactory factory;

    @Autowired
    private CitaService citaService;

    @Autowired
    private ListaEsperaClient listaEsperaClient;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private PrioridadCalculadora prioridadCalculadora;

    /**
     * Procesa la cancelación de una cita y reasigna el horario a otro paciente.
     * <p>
     * Cancela la cita original y utiliza la estrategia de optimización especificada
     * para reasignar el horario a un paciente de la lista de espera.
     * Envía una notificación automática cuando se reasigna la cita.
     * </p>
     *
     * @param citaId el ID de la cita cancelada
     * @param estrategiaTipo el tipo de estrategia de optimización (FIFO, POR_GRAVEDAD)
     */
    public void procesarCancelacion(Long citaId, String estrategiaTipo) {
        citaService.cancelarCita(citaId);
        Cita citaCancelada = citaService.obtenerCitaPorId(citaId).orElse(null);
        if (citaCancelada != null) {
            EstrategiaOptimizacion estrategia = factory.getEstrategia(estrategiaTipo);
            estrategia.reasignarCita(citaCancelada);

            // Notificar reasignación de cita
            try {
                NotificationRequestDTO notif = new NotificationRequestDTO();
                notif.setPacienteId(citaCancelada.getPacienteId());
                notif.setTipo("CITA_REASIGNADA");
                notif.setMensaje("Cita reasignada para " + citaCancelada.getFechaHora());
                notificationClient.createNotification(notif);
                logger.info("Notificación de reasignación enviada para cita {}", citaCancelada.getId());
            } catch (Exception e) {
                logger.warn("Fallo al notificar reasignación de cita {} : {}", citaCancelada.getId(), e.getMessage());
            }
        }
    }

    /**
     * Obtiene la lista de espera del microservicio de gestión de pacientes.
     * <p>
     * Utiliza Circuit Breaker para manejar fallos en la comunicación con el microservicio.
     * </p>
     *
     * @return lista de pacientes en espera
     */
    @CircuitBreaker(name = "listaEsperaService", fallbackMethod = "fallbackListaEspera")
    public List<ListaEsperaDTO> obtenerListaEspera() {
        // Llamada a ms-gestionpacientes usando Feign
        return listaEsperaClient.getListaEspera();
    }

    /**
     * Método de fallback para cuando falla la obtención de la lista de espera.
     * <p>
     * Se activa cuando el Circuit Breaker detecta fallos en la comunicación.
     * </p>
     *
     * @param t la excepción que causó el fallo
     * @return lista vacía
     */
    public List<ListaEsperaDTO> fallbackListaEspera(Throwable t) {
        // Retornar lista vacía o datos locales
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
        return prioridadCalculadora.calcularNivel(gravedad, distanciaKm, diasEspera);
    }
}
