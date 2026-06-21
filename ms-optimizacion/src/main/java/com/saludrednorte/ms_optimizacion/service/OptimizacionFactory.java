package com.saludrednorte.ms_optimizacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory que selecciona la estrategia de reasignacion de citas canceladas.
 */
@Component
public class OptimizacionFactory {

    @Autowired
    private EstrategiaPorGravedad estrategiaPorGravedad;

    @Autowired
    private EstrategiaFIFO estrategiaFIFO;

    /**
     * Obtiene la estrategia de optimizacion segun el tipo indicado.
     *
     * @param tipo tipo de estrategia (fifo, gravedad)
     * @return implementacion de {@link EstrategiaOptimizacion}
     */
    public EstrategiaOptimizacion getEstrategia(String tipo) {
        switch (tipo.toLowerCase()) {
            case "gravedad":
                return estrategiaPorGravedad;
            case "fifo":
                return estrategiaFIFO;
            default:
                return estrategiaFIFO; // default
        }
    }
}
