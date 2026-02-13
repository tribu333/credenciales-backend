package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.unidad.UnidadRequestDTO;
import com.credenciales.tribunal.dto.unidad.UnidadResponseDTO;
import com.credenciales.tribunal.service.UnidadService;
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
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UnidadController {
    
    private final UnidadService unidadService;
    
    @PostMapping
    public ResponseEntity<UnidadResponseDTO> createUnidad(@Valid @RequestBody UnidadRequestDTO requestDTO) {
        UnidadResponseDTO response = unidadService.createUnidad(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UnidadResponseDTO> getUnidadById(@PathVariable Long id) {
        return unidadService.getUnidadById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<UnidadResponseDTO>> getAllUnidades() {
        List<UnidadResponseDTO> unidades = unidadService.getAllUnidades();
        return ResponseEntity.ok(unidades);
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<UnidadResponseDTO>> getAllUnidadesPaged(
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<UnidadResponseDTO> unidades = unidadService.getAllUnidadesPaged(pageable);
        return ResponseEntity.ok(unidades);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UnidadResponseDTO> updateUnidad(
            @PathVariable Long id, 
            @Valid @RequestBody UnidadRequestDTO requestDTO) {
        UnidadResponseDTO response = unidadService.updateUnidad(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnidad(@PathVariable Long id) {
        unidadService.deleteUnidad(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<UnidadResponseDTO>> getUnidadesByEstado(@PathVariable Boolean estado) {
        List<UnidadResponseDTO> unidades = unidadService.getUnidadesByEstado(estado);
        return ResponseEntity.ok(unidades);
    }
    
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<UnidadResponseDTO> getUnidadByNombre(@PathVariable String nombre) {
        return unidadService.getUnidadByNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/abreviatura/{abreviatura}")
    public ResponseEntity<UnidadResponseDTO> getUnidadByAbreviatura(@PathVariable String abreviatura) {
        return unidadService.getUnidadByAbreviatura(abreviatura)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<UnidadResponseDTO>> searchUnidades(@RequestParam String nombre) {
        List<UnidadResponseDTO> unidades = unidadService.searchUnidadesByNombre(nombre);
        return ResponseEntity.ok(unidades);
    }
    
    @PatchMapping("/{id}/estado")
    public ResponseEntity<UnidadResponseDTO> cambiarEstadoUnidad(
            @PathVariable Long id, 
            @RequestParam Boolean estado) {
        UnidadResponseDTO response = unidadService.cambiarEstadoUnidad(id, estado);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/with-cargos")
    public ResponseEntity<UnidadResponseDTO> getUnidadWithCargos(@PathVariable Long id) {
        return unidadService.getUnidadWithCargos(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/with-cargos-proceso")
    public ResponseEntity<UnidadResponseDTO> getUnidadWithCargosProceso(@PathVariable Long id) {
        return unidadService.getUnidadWithCargosProceso(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}