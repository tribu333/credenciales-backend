package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.historialcargo.HistorialCargoCreateRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoSearchRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoUpdateRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoResponseDTO;
import com.credenciales.tribunal.service.HistorialCargoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    
    @GetMapping("/paged")
    public ResponseEntity<Page<HistorialCargoResponseDTO>> getAllHistorialesPaged(
            @PageableDefault(size = 10, sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<HistorialCargoResponseDTO> historiales = historialService.getAllHistorialesPaged(pageable);
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
    
    @GetMapping("/personal/{personalId}")
    public ResponseEntity<List<HistorialCargoResponseDTO>> getHistorialesByPersonal(
            @PathVariable Long personalId) {
        List<HistorialCargoResponseDTO> historiales = historialService.getHistorialesByPersonal(personalId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/cargo/{cargoId}")
    public ResponseEntity<List<HistorialCargoResponseDTO>> getHistorialesByCargo(
            @PathVariable Long cargoId) {
        List<HistorialCargoResponseDTO> historiales = historialService.getHistorialesByCargo(cargoId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/personal/{personalId}/activos")
    public ResponseEntity<List<HistorialCargoResponseDTO>> getHistorialesActivosByPersonal(
            @PathVariable Long personalId) {
        List<HistorialCargoResponseDTO> historiales = historialService.getHistorialesActivosByPersonal(personalId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/cargo/{cargoId}/activos")
    public ResponseEntity<List<HistorialCargoResponseDTO>> getHistorialesActivosByCargo(
            @PathVariable Long cargoId) {
        List<HistorialCargoResponseDTO> historiales = historialService.getHistorialesActivosByCargo(cargoId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/personal/{personalId}/cargo/{cargoId}/activo")
    public ResponseEntity<HistorialCargoResponseDTO> getHistorialActivoByPersonalAndCargo(
            @PathVariable Long personalId, @PathVariable Long cargoId) {
        return historialService.getHistorialActivoByPersonalAndCargo(personalId, cargoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/fechas")
    public ResponseEntity<List<HistorialCargoResponseDTO>> getHistorialesByFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<HistorialCargoResponseDTO> historiales = historialService.getHistorialesByFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/finalizados")
    public ResponseEntity<List<HistorialCargoResponseDTO>> getHistorialesFinalizadosEnRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<HistorialCargoResponseDTO> historiales = historialService.getHistorialesFinalizadosEnRango(start, end);
        return ResponseEntity.ok(historiales);
    }
    
    @PostMapping("/search")
    public ResponseEntity<List<HistorialCargoResponseDTO>> searchHistoriales(
            @RequestBody HistorialCargoSearchRequestDTO searchRequest) {
        List<HistorialCargoResponseDTO> historiales = historialService.searchHistoriales(searchRequest);
        return ResponseEntity.ok(historiales);
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
    
    @GetMapping("/personal/{personalId}/cargo/{cargoId}/exists")
    public ResponseEntity<Boolean> tieneHistorialActivoEnCargo(
            @PathVariable Long personalId, @PathVariable Long cargoId) {
        boolean exists = historialService.tieneHistorialActivoEnCargo(personalId, cargoId);
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/personal/{personalId}/count-activos")
    public ResponseEntity<Long> countHistorialesActivosByPersonal(@PathVariable Long personalId) {
        Long count = historialService.countHistorialesActivosByPersonal(personalId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/cargo/{cargoId}/count-activos")
    public ResponseEntity<Long> countHistorialesActivosByCargo(@PathVariable Long cargoId) {
        Long count = historialService.countHistorialesActivosByCargo(cargoId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/{id}/with-all")
    public ResponseEntity<HistorialCargoResponseDTO> getHistorialWithAllRelations(@PathVariable Long id) {
        return historialService.getHistorialWithAllRelations(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/personal/{personalId}/with-details")
    public ResponseEntity<List<HistorialCargoResponseDTO>> getHistorialesByPersonalWithDetails(
            @PathVariable Long personalId) {
        List<HistorialCargoResponseDTO> historiales = historialService.getHistorialesByPersonalWithDetails(personalId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/cargo/{cargoId}/with-details")
    public ResponseEntity<List<HistorialCargoResponseDTO>> getHistorialesByCargoWithDetails(
            @PathVariable Long cargoId) {
        List<HistorialCargoResponseDTO> historiales = historialService.getHistorialesByCargoWithDetails(cargoId);
        return ResponseEntity.ok(historiales);
    }
}