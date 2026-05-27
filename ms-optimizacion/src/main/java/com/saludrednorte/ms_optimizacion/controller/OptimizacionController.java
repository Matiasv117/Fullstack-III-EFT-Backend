package com.saludrednorte.ms_optimizacion.controller;

import com.saludrednorte.ms_optimizacion.dto.ListaEsperaDTO;
import com.saludrednorte.ms_optimizacion.dto.PrioridadResponse;
import com.saludrednorte.ms_optimizacion.service.OptimizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de optimizacion para cancelaciones, lista de espera y prioridad.
 */
@RestController
@RequestMapping("/optimizacion")
public class OptimizacionController {

    @Autowired
    private OptimizacionService optimizacionService;

    @PostMapping("/cancelar/{citaId}")
    public void procesarCancelacion(@PathVariable Long citaId, @RequestParam(defaultValue = "fifo") String estrategia) {
        optimizacionService.procesarCancelacion(citaId, estrategia);
    }

    @GetMapping("/lista-espera")
    public List<ListaEsperaDTO> obtenerListaEspera() {
        return optimizacionService.obtenerListaEspera();
    }

    /**
     * Calcula la prioridad de un paciente segun gravedad, distancia y dias de espera.
     *
     * @param gravedad nivel 1-5
     * @param distanciaKm distancia geografica en kilometros
     * @param diasEspera dias acumulados en espera
     * @return respuesta con el nivel de prioridad calculado
     */
    @GetMapping("/prioridad")
    public PrioridadResponse calcularPrioridad(@RequestParam int gravedad,
                                               @RequestParam double distanciaKm,
                                               @RequestParam int diasEspera) {
        return new PrioridadResponse(
                optimizacionService.calcularPrioridadPaciente(gravedad, distanciaKm, diasEspera).name()
        );
    }
}
