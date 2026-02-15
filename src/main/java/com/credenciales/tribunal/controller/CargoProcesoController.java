package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoCreateRequestDTO;
//import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoSearchRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoUpdateRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoResponseDTO;
import com.credenciales.tribunal.service.CargoProcesoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/* import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault; */
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
    
}