package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.unidad.UnidadRequestDTO;
import com.credenciales.tribunal.dto.unidad.UnidadResponseDTO;
import com.credenciales.tribunal.service.UnidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Unidades", description = "API para la gestión de unidades")
public class UnidadController {
    
    private final UnidadService unidadService;
    
    @PostMapping
    @Operation(summary = "Crear una nueva unidad", 
               description = "Registra una nueva unidad en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Unidad creada exitosamente",
                    content = @Content(schema = @Schema(implementation = UnidadResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<UnidadResponseDTO> createUnidad(@Valid @RequestBody UnidadRequestDTO requestDTO) {
        UnidadResponseDTO response = unidadService.createUnidad(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una unidad por ID", 
               description = "Retorna una unidad específica basada en su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unidad encontrada"),
        @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<UnidadResponseDTO> getUnidadById(
            @Parameter(description = "ID de la unidad", required = true, example = "1")
            @PathVariable Long id) {
        return unidadService.getUnidadById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Obtener todas las unidades", 
               description = "Retorna una lista de todas las unidades registradas")
    @ApiResponse(responseCode = "200", description = "Lista de unidades obtenida exitosamente")
    public ResponseEntity<List<UnidadResponseDTO>> getAllUnidades() {
        List<UnidadResponseDTO> unidades = unidadService.getAllUnidades();
        return ResponseEntity.ok(unidades);
    }
    
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una unidad", 
    description = "Actualiza los datos de una unidad existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unidad actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<UnidadResponseDTO> updateUnidad(
    @Parameter(description = "ID de la unidad a actualizar", required = true, example = "1")
    @PathVariable Long id, 
    @Valid @RequestBody UnidadRequestDTO requestDTO) {
        UnidadResponseDTO response = unidadService.updateUnidad(id, requestDTO);
        return ResponseEntity.ok(response);
    }
        
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una unidad", 
    description = "Elimina lógicamente una unidad del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Unidad eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<Void> deleteUnidad(
    @Parameter(description = "ID de la unidad a eliminar", required = true, example = "1")
    @PathVariable Long id) {
        unidadService.deleteUnidad(id);
        return ResponseEntity.noContent().build();
    }
    /*     
    @GetMapping("/paged")
    @Operation(summary = "Obtener unidades paginadas", 
                description = "Retorna una lista paginada de unidades con ordenamiento")
    @ApiResponse(responseCode = "200", description = "Página de unidades obtenida exitosamente")
    public ResponseEntity<Page<UnidadResponseDTO>> getAllUnidadesPaged(
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) 
            @Parameter(description = "Parámetros de paginación (page, size, sort)")
            Pageable pageable) {
        Page<UnidadResponseDTO> unidades = unidadService.getAllUnidadesPaged(pageable);
        return ResponseEntity.ok(unidades);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener unidades por estado", 
               description = "Retorna todas las unidades según su estado (activo/inactivo)")
    @ApiResponse(responseCode = "200", description = "Lista de unidades filtrada por estado")
    public ResponseEntity<List<UnidadResponseDTO>> getUnidadesByEstado(
            @Parameter(description = "Estado de la unidad (true=activo, false=inactivo)", 
                      required = true, example = "true")
            @PathVariable Boolean estado) {
        List<UnidadResponseDTO> unidades = unidadService.getUnidadesByEstado(estado);
        return ResponseEntity.ok(unidades);
    }
    
    @GetMapping("/nombre/{nombre}")
    @Operation(summary = "Obtener unidad por nombre exacto", 
               description = "Retorna una unidad específica por su nombre exacto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unidad encontrada"),
        @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<UnidadResponseDTO> getUnidadByNombre(
            @Parameter(description = "Nombre exacto de la unidad", required = true, example = "Secretaría General")
            @PathVariable String nombre) {
        return unidadService.getUnidadByNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/abreviatura/{abreviatura}")
    @Operation(summary = "Obtener unidad por abreviatura", 
               description = "Retorna una unidad específica por su abreviatura")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unidad encontrada"),
        @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<UnidadResponseDTO> getUnidadByAbreviatura(
            @Parameter(description = "Abreviatura de la unidad", required = true, example = "SG")
            @PathVariable String abreviatura) {
        return unidadService.getUnidadByAbreviatura(abreviatura)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    @Operation(summary = "Buscar unidades por nombre", 
               description = "Busca unidades que contengan el texto en su nombre")
    @ApiResponse(responseCode = "200", description = "Lista de unidades que coinciden con la búsqueda")
    public ResponseEntity<List<UnidadResponseDTO>> searchUnidades(
            @Parameter(description = "Texto a buscar en el nombre", required = true, example = "Secretaría")
            @RequestParam String nombre) {
        List<UnidadResponseDTO> unidades = unidadService.searchUnidadesByNombre(nombre);
        return ResponseEntity.ok(unidades);
    }
    
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de una unidad", 
               description = "Activa o desactiva una unidad específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<UnidadResponseDTO> cambiarEstadoUnidad(
            @Parameter(description = "ID de la unidad", required = true, example = "1")
            @PathVariable Long id, 
            @Parameter(description = "Nuevo estado (true=activo, false=inactivo)", 
                      required = true, example = "false")
            @RequestParam Boolean estado) {
        UnidadResponseDTO response = unidadService.cambiarEstadoUnidad(id, estado);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/with-cargos")
    @Operation(summary = "Obtener unidad con sus cargos", 
               description = "Retorna una unidad incluyendo la información de sus cargos asociados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unidad encontrada con sus cargos"),
        @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<UnidadResponseDTO> getUnidadWithCargos(
            @Parameter(description = "ID de la unidad", required = true, example = "1")
            @PathVariable Long id) {
        return unidadService.getUnidadWithCargos(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/with-cargos-proceso")
    @Operation(summary = "Obtener unidad con cargos en proceso", 
               description = "Retorna una unidad incluyendo sus cargos que están en proceso")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unidad encontrada con cargos en proceso"),
        @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<UnidadResponseDTO> getUnidadWithCargosProceso(
            @Parameter(description = "ID de la unidad", required = true, example = "1")
            @PathVariable Long id) {
        return unidadService.getUnidadWithCargosProceso(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    } */
}