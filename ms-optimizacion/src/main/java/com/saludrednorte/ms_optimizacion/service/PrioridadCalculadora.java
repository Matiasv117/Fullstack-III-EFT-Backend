package com.saludrednorte.ms_optimizacion.service;

import org.springframework.stereotype.Component;

/**
 * Calcula el nivel de prioridad de un paciente en base a gravedad,
 * distancia geográfica y tiempo de espera acumulado.
 */
@Component
public class PrioridadCalculadora {

    private static final int GRAVEDAD_MIN = 1;
    private static final int GRAVEDAD_MAX = 5;
    private static final int ESPERA_MAX_DIAS = 60;
    private static final int DISTANCIA_MAX_KM = 100;

    private static final double PESO_GRAVEDAD = 0.50;
    private static final double PESO_ESPERA = 0.35;
    private static final double PESO_DISTANCIA = 0.15;

    /**
     * Retorna el nivel de prioridad con un score 0-100 basado en los parametros.
     *
     * @param gravedad nivel de gravedad (1-5)
     * @param distanciaKm distancia geografica en kilometros (0-100+)
     * @param diasEspera dias acumulados en espera (0-60+)
     * @return nivel de prioridad calculado
     */
    public NivelPrioridad calcularNivel(int gravedad, double distanciaKm, int diasEspera) {
        validarParametros(gravedad, distanciaKm, diasEspera);

        double gravedadScore = normalizar(gravedad, GRAVEDAD_MIN, GRAVEDAD_MAX);
        double esperaScore = normalizar(diasEspera, 0, ESPERA_MAX_DIAS);
        double distanciaScore = normalizar(distanciaKm, 0, DISTANCIA_MAX_KM);

        double scoreTotal = (gravedadScore * PESO_GRAVEDAD)
                + (esperaScore * PESO_ESPERA)
                + (distanciaScore * PESO_DISTANCIA);

        return mapearNivel(scoreTotal);
    }

    private void validarParametros(int gravedad, double distanciaKm, int diasEspera) {
        if (gravedad < GRAVEDAD_MIN || gravedad > GRAVEDAD_MAX) {
            throw new IllegalArgumentException("La gravedad debe estar entre 1 y 5");
        }
        if (distanciaKm < 0) {
            throw new IllegalArgumentException("La distancia no puede ser negativa");
        }
        if (diasEspera < 0) {
            throw new IllegalArgumentException("Los dias de espera no pueden ser negativos");
        }
    }

    private double normalizar(double valor, double min, double max) {
        double clamped = Math.max(min, Math.min(valor, max));
        return ((clamped - min) / (max - min)) * 100.0;
    }

    private NivelPrioridad mapearNivel(double scoreTotal) {
        if (scoreTotal >= 75.0) {
            return NivelPrioridad.ALTA;
        }
        if (scoreTotal >= 40.0) {
            return NivelPrioridad.MEDIA;
        }
        return NivelPrioridad.BAJA;
    }
}
