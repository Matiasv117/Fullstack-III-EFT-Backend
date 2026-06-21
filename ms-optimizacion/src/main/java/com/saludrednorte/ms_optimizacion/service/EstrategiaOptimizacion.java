package com.saludrednorte.ms_optimizacion.service;

import com.saludrednorte.ms_optimizacion.entity.Cita;

/**
 * Contrato para estrategias de reasignacion de citas canceladas.
 */
public interface EstrategiaOptimizacion {
    /**
     * Reasigna una cita cancelada a un paciente de la lista de espera.
     *
     * @param citaCancelada cita que fue cancelada y debe reasignarse
     */
    void reasignarCita(Cita citaCancelada);
}
