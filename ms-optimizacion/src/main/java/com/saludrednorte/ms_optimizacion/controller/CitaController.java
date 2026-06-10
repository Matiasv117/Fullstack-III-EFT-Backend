package com.saludrednorte.ms_optimizacion.controller;

import com.saludrednorte.ms_optimizacion.dto.CitaDTO;
import com.saludrednorte.ms_optimizacion.entity.Cita;
import com.saludrednorte.ms_optimizacion.entity.EstadoCita;
import com.saludrednorte.ms_optimizacion.mapper.ClinicalMapper;
import com.saludrednorte.ms_optimizacion.service.CitaService;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private ClinicalMapper mapper;

    @PostMapping
    public CitaDTO crearCita(@RequestBody CitaDTO citaDTO) {
        Cita cita = mapper.toCitaEntity(citaDTO);
        Cita citaCreada = citaService.crearCita(cita);
        return mapper.toCitaDTO(citaCreada);
    }

    @GetMapping
    public List<CitaDTO> obtenerTodasCitas() {
        return citaService.obtenerTodasCitas().stream()
                .map(mapper::toCitaDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/estado/{estado}")
    public List<CitaDTO> obtenerCitasPorEstado(@PathVariable EstadoCita estado) {
        return citaService.obtenerCitasPorEstado(estado).stream()
                .map(mapper::toCitaDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaDTO> obtenerCitaPorId(@PathVariable Long id) {
        return citaService.obtenerCitaPorId(id)
                .map(cita -> mapper.toCitaDTO(cita))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public CitaDTO actualizarCita(@RequestBody CitaDTO citaDTO) {
        Cita cita = mapper.toCitaEntity(citaDTO);
        Cita citaActualizada = citaService.actualizarCita(cita);
        return mapper.toCitaDTO(citaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return ResponseEntity.ok().build();
    }
}
