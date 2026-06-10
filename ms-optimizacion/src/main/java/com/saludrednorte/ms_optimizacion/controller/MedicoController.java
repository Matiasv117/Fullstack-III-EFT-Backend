package com.saludrednorte.ms_optimizacion.controller;

import com.saludrednorte.ms_optimizacion.dto.MedicoDTO;
import com.saludrednorte.ms_optimizacion.entity.Medico;
import com.saludrednorte.ms_optimizacion.mapper.ClinicalMapper;
import com.saludrednorte.ms_optimizacion.service.MedicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private ClinicalMapper mapper;

    @PostMapping
    public MedicoDTO registrarMedico(@RequestBody MedicoDTO medicoDTO) {
        Medico medico = mapper.toMedicoEntity(medicoDTO);
        Medico medicoRegistrado = medicoService.registrarMedico(medico);
        return mapper.toMedicoDTO(medicoRegistrado);
    }

    @GetMapping
    public List<MedicoDTO> obtenerTodosMedicos() {
        return medicoService.obtenerTodosMedicos().stream()
                .map(mapper::toMedicoDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoDTO> obtenerMedicoPorId(@PathVariable Long id) {
        return medicoService.obtenerMedicoPorId(id)
                .map(mapper::toMedicoDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public MedicoDTO actualizarMedico(@RequestBody MedicoDTO medicoDTO) {
        Medico medico = mapper.toMedicoEntity(medicoDTO);
        Medico medicoActualizado = medicoService.actualizarMedico(medico);
        return mapper.toMedicoDTO(medicoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMedico(@PathVariable Long id) {
        medicoService.eliminarMedico(id);
        return ResponseEntity.ok().build();
    }
}
