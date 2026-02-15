package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralCreateRequestDTO;
//import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralSearchRequestDTO;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralUpdateRequestDTO;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralResponseDTO;
import com.credenciales.tribunal.service.ProcesoElectoralService;
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
//import java.time.LocalDate;
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
    
    @GetMapping("/ultimo")
    public ResponseEntity<ProcesoElectoralResponseDTO> getUltimoProceso() {
        return procesoService.getUltimoProceso()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}