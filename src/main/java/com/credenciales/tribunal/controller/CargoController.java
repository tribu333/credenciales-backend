package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.cargo.CargoRequestDTO;
import com.credenciales.tribunal.dto.cargo.CargoResponseDTO;
import com.credenciales.tribunal.service.CargoService;
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
@RequestMapping("/api/cargos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CargoController {
    
    private final CargoService cargoService;
    
    @PostMapping
    public ResponseEntity<CargoResponseDTO> createCargo(@Valid @RequestBody CargoRequestDTO requestDTO) {
        CargoResponseDTO response = cargoService.createCargo(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CargoResponseDTO> getCargoById(@PathVariable Long id) {
        return cargoService.getCargoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<CargoResponseDTO>> getAllCargos() {
        List<CargoResponseDTO> cargos = cargoService.getAllCargos();
        return ResponseEntity.ok(cargos);
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<CargoResponseDTO>> getAllCargosPaged(
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CargoResponseDTO> cargos = cargoService.getAllCargosPaged(pageable);
        return ResponseEntity.ok(cargos);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CargoResponseDTO> updateCargo(
            @PathVariable Long id, 
            @Valid @RequestBody CargoRequestDTO requestDTO) {
        CargoResponseDTO response = cargoService.updateCargo(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCargo(@PathVariable Long id) {
        cargoService.deleteCargo(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/unidad/{unidadId}")
    public ResponseEntity<List<CargoResponseDTO>> getCargosByUnidad(@PathVariable Long unidadId) {
        List<CargoResponseDTO> cargos = cargoService.getCargosByUnidad(unidadId);
        return ResponseEntity.ok(cargos);
    }
    
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<CargoResponseDTO> getCargoByNombre(@PathVariable String nombre) {
        return cargoService.getCargoByNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CargoResponseDTO>> searchCargos(@RequestParam String nombre) {
        List<CargoResponseDTO> cargos = cargoService.searchCargosByNombre(nombre);
        return ResponseEntity.ok(cargos);
    }
    
    @GetMapping("/{id}/with-historial")
    public ResponseEntity<CargoResponseDTO> getCargoWithHistorial(@PathVariable Long id) {
        return cargoService.getCargoWithHistorial(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/unidad/{unidadId}/with-historial")
    public ResponseEntity<List<CargoResponseDTO>> getCargosByUnidadWithHistorial(@PathVariable Long unidadId) {
        List<CargoResponseDTO> cargos = cargoService.getCargosByUnidadWithHistorial(unidadId);
        return ResponseEntity.ok(cargos);
    }
    
    @GetMapping("/unidad/{unidadId}/count")
    public ResponseEntity<Long> countCargosByUnidad(@PathVariable Long unidadId) {
        Long count = cargoService.countCargosByUnidad(unidadId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsCargoInUnidad(
            @RequestParam String nombre, 
            @RequestParam Long unidadId) {
        boolean exists = cargoService.existsCargoInUnidad(nombre, unidadId);
        return ResponseEntity.ok(exists);
    }
}