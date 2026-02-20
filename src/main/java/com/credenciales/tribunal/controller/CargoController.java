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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/cargos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Cargos", description = "API para gestionar los cargos institucionales")
public class CargoController {
    
    private final CargoService cargoService;
    
    @PostMapping
    @Operation(
        summary = "Crear un nuevo cargo",
        description = "Registra un nuevo cargo institucional en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cargo creado exitosamente",
                    content = @Content(schema = @Schema(implementation = CargoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Ya existe un cargo con ese nombre en la unidad")
    })
    public ResponseEntity<CargoResponseDTO> createCargo(@Valid @RequestBody CargoRequestDTO requestDTO) {
        CargoResponseDTO response = cargoService.createCargo(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener cargo por ID",
        description = "Retorna los detalles de un cargo específico según su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cargo encontrado"),
        @ApiResponse(responseCode = "404", description = "Cargo no encontrado")
    })
    public ResponseEntity<CargoResponseDTO> getCargoById(
            @Parameter(description = "ID del cargo a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return cargoService.getCargoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(
        summary = "Listar todos los cargos",
        description = "Obtiene una lista completa de todos los cargos registrados"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<CargoResponseDTO>> getAllCargos() {
        List<CargoResponseDTO> cargos = cargoService.getAllCargos();
        return ResponseEntity.ok(cargos);
    }
    
    @GetMapping("/paged")
    @Operation(
        summary = "Listar cargos paginados",
        description = "Obtiene una lista paginada de cargos con ordenamiento"
    )
    @ApiResponse(responseCode = "200", description = "Lista paginada obtenida exitosamente")
    public ResponseEntity<Page<CargoResponseDTO>> getAllCargosPaged(
            @Parameter(description = "Parámetros de paginación: page, size, sort")
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CargoResponseDTO> cargos = cargoService.getAllCargosPaged(pageable);
        return ResponseEntity.ok(cargos);
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar un cargo",
        description = "Modifica los datos de un cargo existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cargo actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Cargo no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto con otro cargo existente")
    })
    public ResponseEntity<CargoResponseDTO> updateCargo(
            @Parameter(description = "ID del cargo a actualizar", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody CargoRequestDTO requestDTO) {
        CargoResponseDTO response = cargoService.updateCargo(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar un cargo",
        description = "Elimina lógicamente un cargo del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cargo eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Cargo no encontrado"),
        @ApiResponse(responseCode = "409", description = "No se puede eliminar porque tiene dependencias")
    })
    public ResponseEntity<Void> deleteCargo(
            @Parameter(description = "ID del cargo a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        cargoService.deleteCargo(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/unidad/{unidadId}")
    @Operation(
        summary = "Listar cargos por unidad",
        description = "Obtiene todos los cargos pertenecientes a una unidad específica"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<List<CargoResponseDTO>> getCargosByUnidad(
            @Parameter(description = "ID de la unidad", required = true, example = "1")
            @PathVariable Long unidadId) {
        List<CargoResponseDTO> cargos = cargoService.getCargosByUnidad(unidadId);
        return ResponseEntity.ok(cargos);
    }
    
    @GetMapping("/nombre/{nombre}")
    @Operation(
        summary = "Buscar cargo por nombre exacto",
        description = "Obtiene un cargo específico mediante su nombre exacto"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cargo encontrado"),
        @ApiResponse(responseCode = "404", description = "No existe un cargo con ese nombre")
    })
    public ResponseEntity<CargoResponseDTO> getCargoByNombre(
            @Parameter(description = "Nombre exacto del cargo", required = true, 
                      example = "Coordinador de Logística")
            @PathVariable String nombre) {
        return cargoService.getCargoByNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    @Operation(
        summary = "Buscar cargos por nombre (búsqueda parcial)",
        description = "Busca cargos que contengan el texto especificado en su nombre (búsqueda insensible a mayúsculas/minúsculas)"
    )
    @ApiResponse(responseCode = "200", description = "Lista de cargos que coinciden con la búsqueda")
    public ResponseEntity<List<CargoResponseDTO>> searchCargos(
            @Parameter(description = "Texto a buscar en el nombre del cargo", required = true, 
                      example = "coord")
            @RequestParam String nombre) {
        List<CargoResponseDTO> cargos = cargoService.searchCargosByNombre(nombre);
        return ResponseEntity.ok(cargos);
    }
    
    @GetMapping("/{id}/with-historial")
    @Operation(
        summary = "Obtener cargo con historial de asignaciones",
        description = "Retorna un cargo incluyendo todo su historial de asignaciones a personal"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cargo con historial encontrado"),
        @ApiResponse(responseCode = "404", description = "Cargo no encontrado")
    })
    public ResponseEntity<CargoResponseDTO> getCargoWithHistorial(
            @Parameter(description = "ID del cargo", required = true, example = "1")
            @PathVariable Long id) {
        return cargoService.getCargoWithHistorial(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/unidad/{unidadId}/with-historial")
    @Operation(
        summary = "Listar cargos por unidad con historial",
        description = "Obtiene todos los cargos de una unidad incluyendo sus historiales de asignaciones"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<List<CargoResponseDTO>> getCargosByUnidadWithHistorial(
            @Parameter(description = "ID de la unidad", required = true, example = "1")
            @PathVariable Long unidadId) {
        List<CargoResponseDTO> cargos = cargoService.getCargosByUnidadWithHistorial(unidadId);
        return ResponseEntity.ok(cargos);
    }
    
    @GetMapping("/unidad/{unidadId}/count")
    @Operation(
        summary = "Contar cargos por unidad",
        description = "Retorna el número total de cargos pertenecientes a una unidad específica"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conteo realizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Unidad no encontrada")
    })
    public ResponseEntity<Long> countCargosByUnidad(
            @Parameter(description = "ID de la unidad", required = true, example = "1")
            @PathVariable Long unidadId) {
        Long count = cargoService.countCargosByUnidad(unidadId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/proceso/unidad/{unidadId}")
    public ResponseEntity<List<CargoProcesoResponseDTO>> getCargosProcesoByUnidad(
            @PathVariable Long unidadId) {
        List<CargoProcesoResponseDTO> cargosProceso = cargoService.getAllCargosProcesoByUnidad(unidadId);
        return ResponseEntity.ok(cargosProceso);
    }

    @GetMapping("/proceso")
    public ResponseEntity<List<CargoResponseDTO>> getAllCargosProceso () {
        List <CargoResponseDTO> cargosProceso = cargoService.getAllCargosProceso();
        return ResponseEntity.ok(cargosProceso);
    }

    @GetMapping("/exists")
    @Operation(
        summary = "Verificar si existe un cargo en una unidad",
        description = "Comprueba si ya existe un cargo con el nombre especificado en la unidad indicada"
    )
    @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    public ResponseEntity<Boolean> existsCargoInUnidad(
            @Parameter(description = "Nombre del cargo a verificar", required = true, 
                      example = "Coordinador de Logística")
            @RequestParam String nombre,
            @Parameter(description = "ID de la unidad", required = true, example = "1")
            @RequestParam Long unidadId) {
        boolean exists = cargoService.existsCargoInUnidad(nombre, unidadId);
        return ResponseEntity.ok(exists);
    }
}