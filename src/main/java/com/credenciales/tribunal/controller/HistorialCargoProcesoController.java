package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoCreateRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoSearchRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoUpdateRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoResponseDTO;
import com.credenciales.tribunal.service.HistorialCargoProcesoService;
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
    
    @GetMapping("/paged")
    public ResponseEntity<Page<HistorialCargoProcesoResponseDTO>> getAllHistorialesPaged(
            @PageableDefault(size = 10, sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<HistorialCargoProcesoResponseDTO> historiales = historialService.getAllHistorialesPaged(pageable);
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
    
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesByProceso(
            @PathVariable Long procesoId) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesByProceso(procesoId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/unidad/{unidadId}")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesByUnidad(
            @PathVariable Long unidadId) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesByUnidad(unidadId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/cargo-proceso/{cargoProcesoId}/activos")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesActivosByCargoProceso(
            @PathVariable Long cargoProcesoId) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesActivosByCargoProceso(cargoProcesoId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/personal/{personalId}/activos")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesActivosByPersonal(
            @PathVariable Long personalId) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesActivosByPersonal(personalId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/proceso/{procesoId}/activos")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesActivosByProceso(
            @PathVariable Long procesoId) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesActivosByProceso(procesoId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/personal/{personalId}/cargo-proceso/{cargoProcesoId}/activo")
    public ResponseEntity<HistorialCargoProcesoResponseDTO> getHistorialActivoByPersonalAndCargoProceso(
            @PathVariable Long personalId, @PathVariable Long cargoProcesoId) {
        return historialService.getHistorialActivoByPersonalAndCargoProceso(personalId, cargoProcesoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/rango")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesByRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesByRangoFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/finalizados")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesFinalizadosEnRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesFinalizadosEnRango(start, end);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/sin-fecha-fin")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesSinFechaFin() {
        List<HistorialCargoProcesoResponseDTO> historiales = historialService.getHistorialesSinFechaFin();
        return ResponseEntity.ok(historiales);
    }
    
    @PostMapping("/search")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> searchHistoriales(
            @RequestBody HistorialCargoProcesoSearchRequestDTO searchRequest) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.searchHistoriales(searchRequest);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/{id}/with-all")
    public ResponseEntity<HistorialCargoProcesoResponseDTO> getHistorialWithAllRelations(@PathVariable Long id) {
        return historialService.getHistorialWithAllRelations(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cargo-proceso/{cargoProcesoId}/with-personal")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesByCargoProcesoWithPersonal(
            @PathVariable Long cargoProcesoId) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesByCargoProcesoWithPersonal(cargoProcesoId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/personal/{personalId}/with-cargo-proceso")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesByPersonalWithCargoProceso(
            @PathVariable Long personalId) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesByPersonalWithCargoProceso(personalId);
        return ResponseEntity.ok(historiales);
    }
    
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<HistorialCargoProcesoResponseDTO> finalizarHistorial(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        HistorialCargoProcesoResponseDTO response = historialService.finalizarHistorial(id, fechaFin);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<HistorialCargoProcesoResponseDTO> reactivarHistorial(@PathVariable Long id) {
        HistorialCargoProcesoResponseDTO response = historialService.reactivarHistorial(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/asignar")
    public ResponseEntity<HistorialCargoProcesoResponseDTO> asignarPersonalACargoProceso(
            @RequestParam Long personalId,
            @RequestParam Long cargoProcesoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio) {
        HistorialCargoProcesoResponseDTO response = 
                historialService.asignarPersonalACargoProceso(personalId, cargoProcesoId, fechaInicio);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/{historialId}/reasignar")
    public ResponseEntity<HistorialCargoProcesoResponseDTO> reasignarPersonal(
            @PathVariable Long historialId,
            @RequestParam Long nuevoCargoProcesoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaReasignacion) {
        HistorialCargoProcesoResponseDTO response = 
                historialService.reasignarPersonal(historialId, nuevoCargoProcesoId, fechaReasignacion);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/validar/asignacion")
    public ResponseEntity<Boolean> puedeAsignarPersonalACargoProceso(
            @RequestParam Long personalId,
            @RequestParam Long cargoProcesoId) {
        boolean puede = historialService.puedeAsignarPersonalACargoProceso(personalId, cargoProcesoId);
        return ResponseEntity.ok(puede);
    }
    
    @GetMapping("/personal/{personalId}/cargo-proceso/{cargoProcesoId}/tiene-activo")
    public ResponseEntity<Boolean> tieneHistorialActivoEnCargoProceso(
            @PathVariable Long personalId, @PathVariable Long cargoProcesoId) {
        boolean tiene = historialService.tieneHistorialActivoEnCargoProceso(personalId, cargoProcesoId);
        return ResponseEntity.ok(tiene);
    }
    
    @GetMapping("/cargo-proceso/{cargoProcesoId}/count-activos")
    public ResponseEntity<Long> countHistorialesActivosByCargoProceso(@PathVariable Long cargoProcesoId) {
        Long count = historialService.countHistorialesActivosByCargoProceso(cargoProcesoId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/personal/{personalId}/count-activos")
    public ResponseEntity<Long> countHistorialesActivosByPersonal(@PathVariable Long personalId) {
        Long count = historialService.countHistorialesActivosByPersonal(personalId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/proceso/{procesoId}/count-activos")
    public ResponseEntity<Long> countHistorialesActivosByProceso(@PathVariable Long procesoId) {
        Long count = historialService.countHistorialesActivosByProceso(procesoId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/personal/{personalId}/ultimos")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getUltimosHistorialesByPersonal(
            @PathVariable Long personalId,
            @RequestParam(defaultValue = "5") int limite) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getUltimosHistorialesByPersonal(personalId, limite);
        return ResponseEntity.ok(historiales);
    }
}