package com.saludrednorte.ms_listas_espera.client;

import com.saludrednorte.ms_listas_espera.dto.CitaDTO;
import com.saludrednorte.ms_listas_espera.dto.MedicoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "ms-optimizacion")
public interface CitaClient {

    @PostMapping("/citas")
    ResponseEntity<CitaDTO> crearCita(@RequestBody CitaDTO cita);

    @GetMapping("/citas")
    List<CitaDTO> obtenerTodasCitas();

    @GetMapping("/citas/{id}")
    ResponseEntity<CitaDTO> obtenerCitaPorId(@PathVariable Long id);

    @GetMapping("/citas/estado/{estado}")
    List<CitaDTO> obtenerCitasPorEstado(@PathVariable String estado);

    @PutMapping("/citas")
    ResponseEntity<CitaDTO> actualizarCita(@RequestBody CitaDTO cita);

    @DeleteMapping("/citas/{id}")
    ResponseEntity<Void> cancelarCita(@PathVariable Long id);

    @GetMapping("/medicos")
    List<MedicoDTO> obtenerTodosMedicos();

    @GetMapping("/medicos/{id}")
    ResponseEntity<MedicoDTO> obtenerMedicoPorId(@PathVariable Long id);
}
