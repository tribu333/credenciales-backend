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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/historiales-cargo-proceso")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Historial de Cargos por Proceso Electoral", 
     description = "API para gestionar la asignación de cargos a personal en procesos electorales específicos")
public class HistorialCargoProcesoController {
    
    private final HistorialCargoProcesoService historialService;
    
    @PostMapping
    @Operation(
        summary = "Asignar personal a un cargo en un proceso electoral",
        description = "Registra una nueva asignación de personal a un cargo específico dentro de un proceso electoral"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Asignación creada exitosamente",
                    content = @Content(schema = @Schema(implementation = HistorialCargoProcesoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Personal, cargo o proceso no encontrado"),
        @ApiResponse(responseCode = "409", description = "El personal ya tiene asignado este cargo en el proceso")
    })
    public ResponseEntity<HistorialCargoProcesoResponseDTO> createHistorial(
            @Valid @RequestBody HistorialCargoProcesoCreateRequestDTO requestDTO) {
        HistorialCargoProcesoResponseDTO response = historialService.createHistorial(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener asignación por ID",
        description = "Retorna los detalles de una asignación específica de personal a cargo en proceso electoral"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Asignación encontrada"),
        @ApiResponse(responseCode = "404", description = "Asignación no encontrada")
    })
    public ResponseEntity<HistorialCargoProcesoResponseDTO> getHistorialById(
            @Parameter(description = "ID de la asignación a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return historialService.getHistorialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(
        summary = "Listar todas las asignaciones",
        description = "Obtiene una lista completa de todas las asignaciones de personal a cargos en procesos electorales"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getAllHistoriales() {
        List<HistorialCargoProcesoResponseDTO> historiales = historialService.getAllHistoriales();
        return ResponseEntity.ok(historiales);
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar una asignación",
        description = "Modifica los datos de una asignación existente de personal a cargo en proceso electoral"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Asignación actualizada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Asignación no encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflicto con otra asignación existente")
    })
    public ResponseEntity<HistorialCargoProcesoResponseDTO> updateHistorial(
            @Parameter(description = "ID de la asignación a actualizar", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody HistorialCargoProcesoUpdateRequestDTO requestDTO) {
        HistorialCargoProcesoResponseDTO response = historialService.updateHistorial(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar una asignación",
        description = "Elimina lógicamente una asignación de personal a cargo en proceso electoral"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Asignación eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "Asignación no encontrada")
    })
    public ResponseEntity<Void> deleteHistorial(
            @Parameter(description = "ID de la asignación a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        historialService.deleteHistorial(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/cargo-proceso/{cargoProcesoId}")
    @Operation(
        summary = "Listar asignaciones por cargo-proceso",
        description = "Obtiene todas las asignaciones asociadas a un cargo específico dentro de un proceso electoral"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cargo-Proceso no encontrado")
    })
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesByCargoProceso(
            @Parameter(description = "ID de la relación Cargo-Proceso", required = true, example = "5")
            @PathVariable Long cargoProcesoId) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesByCargoProceso(cargoProcesoId);
        return ResponseEntity.ok(historiales);
    }
    
    @GetMapping("/personal/{personalId}")
    @Operation(
        summary = "Listar asignaciones por personal",
        description = "Obtiene todas las asignaciones de cargos en procesos electorales para un miembro del personal específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    public ResponseEntity<List<HistorialCargoProcesoResponseDTO>> getHistorialesByPersonal(
            @Parameter(description = "ID del miembro del personal", required = true, example = "10")
            @PathVariable Long personalId) {
        List<HistorialCargoProcesoResponseDTO> historiales = 
                historialService.getHistorialesByPersonal(personalId);
        return ResponseEntity.ok(historiales);
    }
}