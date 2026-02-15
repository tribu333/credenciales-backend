package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.historialcargo.HistorialCargoCreateRequestDTO;
//import com.credenciales.tribunal.dto.historialcargo.HistorialCargoSearchRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoUpdateRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoResponseDTO;
import com.credenciales.tribunal.service.HistorialCargoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/* import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault; */
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/historiales-cargo")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HistorialCargoController {
    
    private final HistorialCargoService historialService;
    
    @PostMapping
    public ResponseEntity<HistorialCargoResponseDTO> createHistorial(
            @Valid @RequestBody HistorialCargoCreateRequestDTO requestDTO) {
        HistorialCargoResponseDTO response = historialService.createHistorial(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<HistorialCargoResponseDTO> getHistorialById(@PathVariable Long id) {
        return historialService.getHistorialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<HistorialCargoResponseDTO>> getAllHistoriales() {
        List<HistorialCargoResponseDTO> historiales = historialService.getAllHistoriales();
        return ResponseEntity.ok(historiales);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<HistorialCargoResponseDTO> updateHistorial(
            @PathVariable Long id, 
            @Valid @RequestBody HistorialCargoUpdateRequestDTO requestDTO) {
        HistorialCargoResponseDTO response = historialService.updateHistorial(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistorial(@PathVariable Long id) {
        historialService.deleteHistorial(id);
        return ResponseEntity.noContent().build();
    }
        
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<HistorialCargoResponseDTO> finalizarHistorial(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        HistorialCargoResponseDTO response = historialService.finalizarHistorial(id, fechaFin);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<HistorialCargoResponseDTO> reactivarHistorial(@PathVariable Long id) {
        HistorialCargoResponseDTO response = historialService.reactivarHistorial(id);
        return ResponseEntity.ok(response);
    }
    
}