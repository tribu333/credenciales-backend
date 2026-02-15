package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoCreateRequestDTO;
//import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoSearchRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoUpdateRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoResponseDTO;
import com.credenciales.tribunal.service.HistorialCargoProcesoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/* import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat; */
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/historiales-cargo-proceso")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HistorialCargoProcesoController {
    
    private final HistorialCargoProcesoService historialService;
    
    @PostMapping
    public ResponseEntity<HistorialCargoProcesoResponseDTO> createHistorial(
            @Valid @RequestBody HistorialCargoProcesoCreateRequestDTO requestDTO) {
        HistorialCargoProcesoResponseDTO response = historialService.createHistorial(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<HistorialCargoProcesoResponseDTO> getHistorialById(@PathVariable Long id) {
        return historialService.getHistorialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getAllHistoriales() {
        List<HistorialCargoProcesoResponseDTO> historiales = historialService.getAllHistoriales();
        return ResponseEntity.ok(historiales);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<HistorialCargoProcesoResponseDTO> updateHistorial(
            @PathVariable Long id, 
            @Valid @RequestBody HistorialCargoProcesoUpdateRequestDTO requestDTO) {
        HistorialCargoProcesoResponseDTO response = historialService.updateHistorial(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistorial(@PathVariable Long id) {
        historialService.deleteHistorial(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/cargo-proceso/{cargoProcesoId}")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesByCargoProceso(
            @PathVariable Long cargoProcesoId) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesByCargoProceso(cargoProcesoId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/personal/{personalId}")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesByPersonal(
            @PathVariable Long personalId) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesByPersonal(personalId);
        return ResponseEntity.ok(historiales);
    }
    
}