package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.cargo.CargoResponseDTO;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/cargos-proceso")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Cargos por Proceso Electoral", 
     description = "API para gestionar los cargos disponibles en cada proceso electoral")
public class CargoProcesoController {
    
    private final CargoProcesoService cargoProcesoService;
    
    @PostMapping
    @Operation(
        summary = "Asociar un cargo a un proceso electoral",
        description = "Registra un nuevo cargo disponible en un proceso electoral específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cargo-Proceso creado exitosamente",
                    content = @Content(schema = @Schema(implementation = CargoProcesoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Cargo o Proceso electoral no encontrado"),
        @ApiResponse(responseCode = "409", description = "El cargo ya está asociado a este proceso")
    })
    public ResponseEntity<CargoProcesoResponseDTO> createCargoProceso(
            @Valid @RequestBody CargoProcesoCreateRequestDTO requestDTO) {
        CargoProcesoResponseDTO response = cargoProcesoService.createCargoProceso(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/batch-simple")
public ResponseEntity<List<CargoProcesoResponseDTO>> createCargosSimple(
        @Valid @RequestBody List<CargoProcesoCreateRequestDTO> cargos) {
    
    List<CargoProcesoResponseDTO> responses = cargoProcesoService.createCargosSimple(cargos);
    return new ResponseEntity<>(responses, HttpStatus.CREATED);
}
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener asociación Cargo-Proceso por ID",
        description = "Retorna los detalles de una asociación específica entre un cargo y un proceso electoral"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Asociación encontrada"),
        @ApiResponse(responseCode = "404", description = "Asociación no encontrada")
    })
    public ResponseEntity<CargoProcesoResponseDTO> getCargoProcesoById(
            @Parameter(description = "ID de la asociación Cargo-Proceso a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return cargoProcesoService.getCargoProcesoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @Operation(
        summary = "Obtener asociación Cargo-Proceso por unidadID",
        description = "Retorna los procesos de la unidad"
    )
    @GetMapping("/unidad/{unidadId}")
    public ResponseEntity<List<CargoProcesoResponseDTO>> getCargosProcesoByUnidad(
            @PathVariable Long unidadId) {
        List<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.getCargosProcesoByUnidad(unidadId);
        return ResponseEntity.ok(cargosProceso);
    }
    @GetMapping
    @Operation(
        summary = "Listar todas las asociaciones Cargo-Proceso",
        description = "Obtiene una lista completa de todos los cargos asociados a procesos electorales"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<CargoProcesoResponseDTO>> getAllCargosProceso() {
        List<CargoProcesoResponseDTO> cargosProceso = cargoProcesoService.getAllCargosProceso();
        return ResponseEntity.ok(cargosProceso);
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar una asociación Cargo-Proceso",
        description = "Modifica los datos de una asociación existente entre cargo y proceso electoral"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Asociación actualizada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Asociación no encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflicto con otra asociación existente")
    })
    public ResponseEntity<CargoProcesoResponseDTO> updateCargoProceso(
            @Parameter(description = "ID de la asociación a actualizar", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody CargoProcesoUpdateRequestDTO requestDTO) {
        CargoProcesoResponseDTO response = cargoProcesoService.updateCargoProceso(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar una asociación Cargo-Proceso",
        description = "Elimina lógicamente la asociación entre un cargo y un proceso electoral"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Asociación eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "Asociación no encontrada"),
        @ApiResponse(responseCode = "409", description = "No se puede eliminar porque tiene asignaciones de personal activas")
    })
    public ResponseEntity<Void> deleteCargoProceso(
            @Parameter(description = "ID de la asociación a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        cargoProcesoService.deleteCargoProceso(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/activar")
    @Operation(
        summary = "Activar un cargo en el proceso",
        description = "Activa un cargo para que pueda ser asignado a personal en el proceso electoral"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cargo activado correctamente"),
        @ApiResponse(responseCode = "404", description = "Asociación no encontrada"),
        @ApiResponse(responseCode = "400", description = "El cargo ya está activo")
    })
    public ResponseEntity<CargoProcesoResponseDTO> activarCargoProceso(
            @Parameter(description = "ID de la asociación a activar", required = true, example = "1")
            @PathVariable Long id) {
        CargoProcesoResponseDTO response = cargoProcesoService.activarCargoProceso(id);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/desactivar")
    @Operation(
        summary = "Desactivar un cargo en el proceso",
        description = "Desactiva un cargo para que no pueda ser asignado a personal en el proceso electoral"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cargo desactivado correctamente"),
        @ApiResponse(responseCode = "404", description = "Asociación no encontrada"),
        @ApiResponse(responseCode = "400", description = "El cargo ya está desactivado o tiene asignaciones activas")
    })
    public ResponseEntity<CargoProcesoResponseDTO> desactivarCargoProceso(
            @Parameter(description = "ID de la asociación a desactivar", required = true, example = "1")
            @PathVariable Long id) {
        CargoProcesoResponseDTO response = cargoProcesoService.desactivarCargoProceso(id);
        return ResponseEntity.ok(response);
    }
}