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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/historiales-cargo")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Historial de Cargos", description = "API para gestionar el historial de cargos de empleados")
public class HistorialCargoController {
    
    private final HistorialCargoService historialService;
    
    @PostMapping
    @Operation(
        summary = "Crear un nuevo historial de cargo",
        description = "Registra un nuevo cargo para un empleado en una fecha específica"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Historial creado exitosamente",
                    content = @Content(schema = @Schema(implementation = HistorialCargoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    public ResponseEntity<HistorialCargoResponseDTO> createHistorial(
            @Valid @RequestBody HistorialCargoCreateRequestDTO requestDTO) {
        HistorialCargoResponseDTO response = historialService.createHistorial(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener historial por ID",
        description = "Retorna los detalles de un historial de cargo específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historial encontrado"),
        @ApiResponse(responseCode = "404", description = "Historial no encontrado")
    })
    public ResponseEntity<HistorialCargoResponseDTO> getHistorialById(
            @Parameter(description = "ID del historial a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return historialService.getHistorialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(
        summary = "Listar todos los historiales",
        description = "Obtiene una lista completa de todos los historiales de cargo registrados"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<HistorialCargoResponseDTO>> getAllHistoriales() {
        List<HistorialCargoResponseDTO> historiales = historialService.getAllHistoriales();
        return ResponseEntity.ok(historiales);
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar un historial",
        description = "Modifica los datos de un historial de cargo existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historial actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Historial no encontrado")
    })
    public ResponseEntity<HistorialCargoResponseDTO> updateHistorial(
            @Parameter(description = "ID del historial a actualizar", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody HistorialCargoUpdateRequestDTO requestDTO) {
        HistorialCargoResponseDTO response = historialService.updateHistorial(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar un historial",
        description = "Elimina lógicamente un historial de cargo (soft delete)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Historial eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Historial no encontrado")
    })
    public ResponseEntity<Void> deleteHistorial(
            @Parameter(description = "ID del historial a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        historialService.deleteHistorial(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/finalizar")
    @Operation(
        summary = "Finalizar un historial",
        description = "Marca un historial de cargo como finalizado con la fecha especificada"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historial finalizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Historial no encontrado"),
        @ApiResponse(responseCode = "400", description = "El historial ya está finalizado o fecha inválida")
    })
    public ResponseEntity<HistorialCargoResponseDTO> finalizarHistorial(
            @Parameter(description = "ID del historial a finalizar", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Fecha de finalización (formato ISO: yyyy-MM-dd'T'HH:mm:ss)", 
                      example = "2024-01-15T14:30:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        HistorialCargoResponseDTO response = historialService.finalizarHistorial(id, fechaFin);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/reactivar")
    @Operation(
        summary = "Reactivar un historial",
        description = "Reactivar un historial de cargo que estaba finalizado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historial reactivado correctamente"),
        @ApiResponse(responseCode = "404", description = "Historial no encontrado"),
        @ApiResponse(responseCode = "400", description = "El historial no está finalizado")
    })
    public ResponseEntity<HistorialCargoResponseDTO> reactivarHistorial(
            @Parameter(description = "ID del historial a reactivar", required = true, example = "1")
            @PathVariable Long id) {
        HistorialCargoResponseDTO response = historialService.reactivarHistorial(id);
        return ResponseEntity.ok(response);
    }
}