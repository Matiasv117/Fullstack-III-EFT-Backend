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

    @Autowired
    private EstrategiaLIFO estrategiaLIFO;

    /**
     * Obtiene la estrategia de optimizacion segun el tipo indicado.
     *
     * @param tipo tipo de estrategia (fifo, lifo, gravedad)
     * @return implementacion de {@link EstrategiaOptimizacion}
     */
    public EstrategiaOptimizacion getEstrategia(String tipo) {
        switch (tipo.toLowerCase()) {
            case "gravedad":
                return estrategiaPorGravedad;
            case "lifo":
                return estrategiaLIFO;
            case "fifo":
                return estrategiaFIFO;
            default:
                return estrategiaFIFO;
        }
    }
}
