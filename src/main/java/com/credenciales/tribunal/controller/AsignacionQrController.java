package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.asignacionesqr.AsignacionRequestDTO;
import com.credenciales.tribunal.dto.asignacionesqr.AsignacionResponseDTO;
import com.credenciales.tribunal.service.AsignacionQrService;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/asignaciones-qr")
@RequiredArgsConstructor
@Tag(name = "Asignaciones QR", description = "Endpoints para gestionar asignaciones de QR a externos")
@CrossOrigin(origins = "*")
public class AsignacionQrController {

    private final AsignacionQrService asignacionQrService;

    // ==================== MÉTODOS GET ====================

    @Hidden
    @GetMapping
    @Operation(summary = "Obtener todas las asignaciones", 
               description = "Retorna una lista de todas las asignaciones QR")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<AsignacionResponseDTO>> findAll() {
        log.info("GET /api/v1/asignaciones-qr - Listando todas las asignaciones");
        List<AsignacionResponseDTO> asignaciones = asignacionQrService.findAll();
        return ResponseEntity.ok(asignaciones);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener asignación por ID", 
               description = "Retorna una asignación específica basada en su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Asignación encontrada"),
        @ApiResponse(responseCode = "404", description = "Asignación no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<AsignacionResponseDTO> findById(
            @Parameter(description = "ID de la asignación", required = true)
            @PathVariable Long id) {
        log.info("GET /api/v1/asignaciones-qr/{} - Buscando asignación por ID", id);
        AsignacionResponseDTO asignacion = asignacionQrService.findById(id);
        return ResponseEntity.ok(asignacion);
    }

    @GetMapping("/activas")
    @Operation(summary = "Obtener todas las asignaciones activas", 
               description = "Retorna una lista de todas las asignaciones QR activas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<AsignacionResponseDTO>> findByActivoTrue() {
        log.info("GET /api/v1/asignaciones-qr/activas - Listando asignaciones activas");
        List<AsignacionResponseDTO> asignaciones = asignacionQrService.findByActivoTrue();
        return ResponseEntity.ok(asignaciones);
    }

    @Hidden
    @GetMapping("/externo/{externoId}")
    @Operation(summary = "Obtener asignaciones por ID de externo", 
               description = "Retorna todas las asignaciones de un externo específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Externo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<AsignacionResponseDTO>> findByExternoId(
            @Parameter(description = "ID del externo", required = true)
            @PathVariable Long externoId) {
        log.info("GET /api/v1/asignaciones-qr/externo/{} - Buscando asignaciones por externo", externoId);
        List<AsignacionResponseDTO> asignaciones = asignacionQrService.findByExternoId(externoId);
        return ResponseEntity.ok(asignaciones);
    }

    @Hidden
    @GetMapping("/externo/{externoId}/activas")
    @Operation(summary = "Obtener asignaciones activas por ID de externo", 
               description = "Retorna las asignaciones activas de un externo específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Externo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<AsignacionResponseDTO>> findByExternoIdAndActivoTrue(
            @Parameter(description = "ID del externo", required = true)
            @PathVariable Long externoId) {
        log.info("GET /api/v1/asignaciones-qr/externo/{}/activas - Buscando asignaciones activas por externo", externoId);
        List<AsignacionResponseDTO> asignaciones = asignacionQrService.findByExternoIdAndActivoTrue(externoId);
        return ResponseEntity.ok(asignaciones);
    }

    @GetMapping("/externo/{externoId}/activa")
    @Operation(summary = "Obtener la asignación activa de un externo", 
               description = "Retorna la única asignación activa de un externo específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Asignación encontrada"),
        @ApiResponse(responseCode = "404", description = "Externo no encontrado o sin asignación activa"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<AsignacionResponseDTO> findActivaByExternoId(
            @Parameter(description = "ID del externo", required = true)
            @PathVariable Long externoId) {
        log.info("GET /api/v1/asignaciones-qr/externo/{}/activa - Buscando asignación activa por externo", externoId);
        AsignacionResponseDTO asignacion = asignacionQrService.findActivaByExternoId(externoId);
        return ResponseEntity.ok(asignacion);
    }

    @GetMapping("/externo/{externoId}/existe-activa")
    @Operation(summary = "Verificar si externo tiene asignación activa", 
               description = "Verifica si un externo tiene alguna asignación activa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificación exitosa"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Map<String, Boolean>> existsByExternoIdAndActivoTrue(
            @Parameter(description = "ID del externo", required = true)
            @PathVariable Long externoId) {
        log.info("GET /api/v1/asignaciones-qr/externo/{}/existe-activa - Verificando si externo tiene asignación activa", externoId);
        
        boolean existe = asignacionQrService.existsByExternoIdAndActivoTrue(externoId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("tieneAsignacionActiva", existe);
        
        return ResponseEntity.ok(response);
    }

    // ==================== MÉTODOS POST ====================

    @PostMapping
    @Operation(summary = "Crear nueva asignación", 
               description = "Crea una nueva asignación de QR a un externo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Asignación creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o QR/Externo no disponible"),
        @ApiResponse(responseCode = "404", description = "QR o Externo no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto - QR o Externo ya tienen asignación activa"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<AsignacionResponseDTO> create(
            @Parameter(description = "Datos de la asignación", required = true)
            @Valid @RequestBody AsignacionRequestDTO requestDTO) {
        log.info("POST /api/v1/asignaciones-qr - Creando nueva asignación: {}", requestDTO);
        
        AsignacionResponseDTO createdAsignacion = asignacionQrService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAsignacion);
    }

    // ==================== MÉTODOS PUT ====================
    
    @Hidden
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar asignación existente", 
               description = "Actualiza una asignación existente por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Asignación actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Asignación, QR o Externo no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto - QR o Externo ya tienen asignación activa"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<AsignacionResponseDTO> update(
            @Parameter(description = "ID de la asignación a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos de la asignación", required = true)
            @Valid @RequestBody AsignacionRequestDTO requestDTO) {
        log.info("PUT /api/v1/asignaciones-qr/{} - Actualizando asignación: {}", id, requestDTO);
        
        AsignacionResponseDTO updatedAsignacion = asignacionQrService.update(id, requestDTO);
        return ResponseEntity.ok(updatedAsignacion);
    }

    @PutMapping("/{id}/liberar")
    @Operation(summary = "Liberar asignación", 
               description = "Marca una asignación como liberada (inactiva) y libera el QR asociado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Asignación liberada exitosamente"),
        @ApiResponse(responseCode = "400", description = "La asignación ya estaba liberada"),
        @ApiResponse(responseCode = "404", description = "Asignación no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<AsignacionResponseDTO> liberarAsignacion(
            @Parameter(description = "ID de la asignación a liberar", required = true)
            @PathVariable Long id) {
        log.info("PUT /api/v1/asignaciones-qr/{}/liberar - Liberando asignación", id);
        
        AsignacionResponseDTO liberatedAsignacion = asignacionQrService.liberarAsignacion(id);
        return ResponseEntity.ok(liberatedAsignacion);
    }

    // ==================== MÉTODOS DELETE ====================

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar asignación (soft delete)", 
               description = "Realiza un soft delete de la asignación, marcándola como inactiva")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Asignación eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Asignación no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "ID de la asignación a eliminar", required = true)
            @PathVariable Long id) {
        log.info("DELETE /api/v1/asignaciones-qr/{} - Eliminando asignación (soft delete)", id);
        
        asignacionQrService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}