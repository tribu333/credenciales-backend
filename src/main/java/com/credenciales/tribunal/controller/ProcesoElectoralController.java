package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralCreateRequestDTO;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralSearchRequestDTO;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralUpdateRequestDTO;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralResponseDTO;
import com.credenciales.tribunal.service.ProcesoElectoralService;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/procesos-electorales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProcesoElectoralController {
    
    private final ProcesoElectoralService procesoService;
    
    @PostMapping
    public ResponseEntity<ProcesoElectoralResponseDTO> createProceso(
            @Valid @RequestBody ProcesoElectoralCreateRequestDTO requestDTO) {
        ProcesoElectoralResponseDTO response = procesoService.createProceso(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProcesoElectoralResponseDTO> getProcesoById(@PathVariable Long id) {
        return procesoService.getProcesoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<ProcesoElectoralResponseDTO>> getAllProcesos() {
        List<ProcesoElectoralResponseDTO> procesos = procesoService.getAllProcesos();
        return ResponseEntity.ok(procesos);
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<ProcesoElectoralResponseDTO>> getAllProcesosPaged(
            @PageableDefault(size = 10, sort = "fechaInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProcesoElectoralResponseDTO> procesos = procesoService.getAllProcesosPaged(pageable);
        return ResponseEntity.ok(procesos);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProcesoElectoralResponseDTO> updateProceso(
            @PathVariable Long id, 
            @Valid @RequestBody ProcesoElectoralUpdateRequestDTO requestDTO) {
        ProcesoElectoralResponseDTO response = procesoService.updateProceso(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProceso(@PathVariable Long id) {
        procesoService.deleteProceso(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<ProcesoElectoralResponseDTO> getProcesoByNombre(@PathVariable String nombre) {
        return procesoService.getProcesoByNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ProcesoElectoralResponseDTO>> searchProcesos(@RequestParam String nombre) {
        List<ProcesoElectoralResponseDTO> procesos = procesoService.searchProcesosByNombre(nombre);
        return ResponseEntity.ok(procesos);
    }
    
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ProcesoElectoralResponseDTO>> getProcesosByEstado(@PathVariable Boolean estado) {
        List<ProcesoElectoralResponseDTO> procesos = procesoService.getProcesosByEstado(estado);
        return ResponseEntity.ok(procesos);
    }
    
    @GetMapping("/activos")
    public ResponseEntity<List<ProcesoElectoralResponseDTO>> getProcesosActivos() {
        List<ProcesoElectoralResponseDTO> procesos = procesoService.getProcesosActivos();
        return ResponseEntity.ok(procesos);
    }
    
    @GetMapping("/vigentes")
    public ResponseEntity<List<ProcesoElectoralResponseDTO>> getProcesosVigentesActuales() {
        List<ProcesoElectoralResponseDTO> procesos = procesoService.getProcesosVigentesActuales();
        return ResponseEntity.ok(procesos);
    }
    
    @GetMapping("/proximos")
    public ResponseEntity<List<ProcesoElectoralResponseDTO>> getProcesosProximos() {
        List<ProcesoElectoralResponseDTO> procesos = procesoService.getProcesosProximos();
        return ResponseEntity.ok(procesos);
    }
    
    @GetMapping("/finalizados")
    public ResponseEntity<List<ProcesoElectoralResponseDTO>> getProcesosFinalizados() {
        List<ProcesoElectoralResponseDTO> procesos = procesoService.getProcesosFinalizados();
        return ResponseEntity.ok(procesos);
    }
    
    @GetMapping("/rango")
    public ResponseEntity<List<ProcesoElectoralResponseDTO>> getProcesosByRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        List<ProcesoElectoralResponseDTO> procesos = procesoService.getProcesosByRangoFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(procesos);
    }
    
    @GetMapping("/vigentes/{fecha}")
    public ResponseEntity<List<ProcesoElectoralResponseDTO>> getProcesosVigentes(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<ProcesoElectoralResponseDTO> procesos = procesoService.getProcesosVigentes(fecha);
        return ResponseEntity.ok(procesos);
    }
    
    @PostMapping("/search/advanced")
    public ResponseEntity<List<ProcesoElectoralResponseDTO>> searchProcesosAdvanced(
            @RequestBody ProcesoElectoralSearchRequestDTO searchRequest) {
        List<ProcesoElectoralResponseDTO> procesos = procesoService.searchProcesos(searchRequest);
        return ResponseEntity.ok(procesos);
    }
    
    @GetMapping("/{id}/with-imagen")
    public ResponseEntity<ProcesoElectoralResponseDTO> getProcesoWithImagen(@PathVariable Long id) {
        return procesoService.getProcesoWithImagen(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /* @GetMapping("/{id}/with-cargos")
    public ResponseEntity<ProcesoElectoralResponseDTO> getProcesoWithCargos(@PathVariable Long id) {
        return procesoService.getProcesoWithCargos(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    } */
    
    @GetMapping("/{id}/with-all")
    public ResponseEntity<ProcesoElectoralResponseDTO> getProcesoWithAllRelations(@PathVariable Long id) {
        return procesoService.getProcesoWithAllRelations(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/{id}/activar")
    public ResponseEntity<ProcesoElectoralResponseDTO> activarProceso(@PathVariable Long id) {
        ProcesoElectoralResponseDTO response = procesoService.activarProceso(id);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProcesoElectoralResponseDTO> desactivarProceso(@PathVariable Long id) {
        ProcesoElectoralResponseDTO response = procesoService.desactivarProceso(id);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/extender-fecha")
    public ResponseEntity<ProcesoElectoralResponseDTO> extenderFechaFin(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nuevaFechaFin) {
        ProcesoElectoralResponseDTO response = procesoService.extenderFechaFin(id, nuevaFechaFin);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/vigente")
    public ResponseEntity<Boolean> isProcesoVigente(@PathVariable Long id) {
        boolean vigente = procesoService.isProcesoVigente(id);
        return ResponseEntity.ok(vigente);
    }
    
    @GetMapping("/count/estado/{estado}")
    public ResponseEntity<Long> countProcesosByEstado(@PathVariable Boolean estado) {
        Long count = procesoService.countProcesosByEstado(estado);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/ultimo")
    public ResponseEntity<ProcesoElectoralResponseDTO> getUltimoProceso() {
        return procesoService.getUltimoProceso()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}