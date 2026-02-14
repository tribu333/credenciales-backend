package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoCreateRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoSearchRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoUpdateRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoResponseDTO;
import com.credenciales.tribunal.service.CargoProcesoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cargos-proceso")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CargoProcesoController {
    
    private final CargoProcesoService cargoProcesoService;
    
    @PostMapping
    public ResponseEntity<CargoProcesoResponseDTO> createCargoProceso(
            @Valid @RequestBody CargoProcesoCreateRequestDTO requestDTO) {
        CargoProcesoResponseDTO response = cargoProcesoService.createCargoProceso(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CargoProcesoResponseDTO> getCargoProcesoById(@PathVariable Long id) {
        return cargoProcesoService.getCargoProcesoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<CargoProcesoResponseDTO>> getAllCargosProceso() {
        List<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.getAllCargosProceso();
        return ResponseEntity.ok(cargosProceso);
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<CargoProcesoResponseDTO>> getAllCargosProcesoPaged(
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.getAllCargosProcesoPaged(pageable);
        return ResponseEntity.ok(cargosProceso);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CargoProcesoResponseDTO> updateCargoProceso(
            @PathVariable Long id, 
            @Valid @RequestBody CargoProcesoUpdateRequestDTO requestDTO) {
        CargoProcesoResponseDTO response = cargoProcesoService.updateCargoProceso(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCargoProceso(@PathVariable Long id) {
        cargoProcesoService.deleteCargoProceso(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<List<CargoProcesoResponseDTO>> getCargosProcesoByProceso(
            @PathVariable Long procesoId) {
        List<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.getCargosProcesoByProceso(procesoId);
        return ResponseEntity.ok(cargosProceso);
    }
    
    @GetMapping("/unidad/{unidadId}")
    public ResponseEntity<List<CargoProcesoResponseDTO>> getCargosProcesoByUnidad(
            @PathVariable Long unidadId) {
        List<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.getCargosProcesoByUnidad(unidadId);
        return ResponseEntity.ok(cargosProceso);
    }
    
    @GetMapping("/proceso/{procesoId}/activos")
    public ResponseEntity<List<CargoProcesoResponseDTO>> getCargosProcesoActivosByProceso(
            @PathVariable Long procesoId) {
        List<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.getCargosProcesoActivosByProceso(procesoId);
        return ResponseEntity.ok(cargosProceso);
    }
    
    @GetMapping("/proceso/{procesoId}/unidad/{unidadId}")
    public ResponseEntity<List<CargoProcesoResponseDTO>> getCargosProcesoByProcesoAndUnidad(
            @PathVariable Long procesoId, @PathVariable Long unidadId) {
        List<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.getCargosProcesoByProcesoAndUnidad(procesoId, unidadId);
        return ResponseEntity.ok(cargosProceso);
    }
    
    @GetMapping("/proceso/{procesoId}/nombre/{nombre}")
    public ResponseEntity<CargoProcesoResponseDTO> getCargoProcesoByProcesoAndNombre(
            @PathVariable Long procesoId, @PathVariable String nombre) {
        return cargoProcesoService.getCargoProcesoByProcesoAndNombre(procesoId, nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CargoProcesoResponseDTO>> searchCargosProceso(@RequestParam String nombre) {
        List<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.searchCargosProcesoByNombre(nombre);
        return ResponseEntity.ok(cargosProceso);
    }
    
    @PostMapping("/search/advanced")
    public ResponseEntity<List<CargoProcesoResponseDTO>> searchCargosProcesoAdvanced(
            @RequestBody CargoProcesoSearchRequestDTO searchRequest) {
        List<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.searchCargosProceso(searchRequest);
        return ResponseEntity.ok(cargosProceso);
    }
    
    @GetMapping("/{id}/with-all")
    public ResponseEntity<CargoProcesoResponseDTO> getCargoProcesoWithAllRelations(@PathVariable Long id) {
        return cargoProcesoService.getCargoProcesoWithAllRelations(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/proceso/{procesoId}/with-relations")
    public ResponseEntity<List<CargoProcesoResponseDTO>> getCargosProcesoByProcesoWithRelations(
            @PathVariable Long procesoId) {
        List<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.getCargosProcesoByProcesoWithRelations(procesoId);
        return ResponseEntity.ok(cargosProceso);
    }
    
    @GetMapping("/proceso/{procesoId}/with-historial-count")
    public ResponseEntity<List<CargoProcesoResponseDTO>> getCargosProcesoWithHistorialCount(
            @PathVariable Long procesoId) {
        List<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.getCargosProcesoWithHistorialCount(procesoId);
        return ResponseEntity.ok(cargosProceso);
    }
    
    @PatchMapping("/{id}/activar")
    public ResponseEntity<CargoProcesoResponseDTO> activarCargoProceso(@PathVariable Long id) {
        CargoProcesoResponseDTO response = cargoProcesoService.activarCargoProceso(id);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<CargoProcesoResponseDTO> desactivarCargoProceso(@PathVariable Long id) {
        CargoProcesoResponseDTO response = cargoProcesoService.desactivarCargoProceso(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/tiene-historiales-activos")
    public ResponseEntity<Boolean> tieneHistorialesActivos(@PathVariable Long id) {
        boolean tiene = cargoProcesoService.tieneHistorialesActivos(id);
        return ResponseEntity.ok(tiene);
    }
    
    @GetMapping("/proceso/{procesoId}/count")
    public ResponseEntity<Long> countCargosProcesoByProceso(@PathVariable Long procesoId) {
        Long count = cargoProcesoService.countCargosProcesoByProceso(procesoId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/unidad/{unidadId}/count")
    public ResponseEntity<Long> countCargosProcesoByUnidad(@PathVariable Long unidadId) {
        Long count = cargoProcesoService.countCargosProcesoByUnidad(unidadId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsCargoProcesoInProceso(
            @RequestParam Long procesoId, 
            @RequestParam String nombre) {
        boolean exists = cargoProcesoService.existsCargoProcesoInProceso(procesoId, nombre);
        return ResponseEntity.ok(exists);
    }
}