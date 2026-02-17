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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/procesos-electorales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Procesos Electorales", description = "API para gestionar los procesos electorales")
public class ProcesoElectoralController {
    
    private final ProcesoElectoralService procesoService;
    
    @PostMapping
    @Operation(
        summary = "Crear un nuevo proceso electoral",
        description = "Registra un nuevo proceso electoral en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Proceso electoral creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProcesoElectoralResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Ya existe un proceso con ese nombre")
    })
    public ResponseEntity<ProcesoElectoralResponseDTO> createProceso(
            @Valid @RequestBody ProcesoElectoralCreateRequestDTO requestDTO) {
        ProcesoElectoralResponseDTO response = procesoService.createProceso(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener proceso electoral por ID",
        description = "Retorna los detalles de un proceso electoral específico según su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proceso electoral encontrado"),
        @ApiResponse(responseCode = "404", description = "Proceso electoral no encontrado")
    })
    public ResponseEntity<ProcesoElectoralResponseDTO> getProcesoById(
            @Parameter(description = "ID del proceso electoral a buscar", required = true, example = "1")
            @PathVariable Long id) {
        return procesoService.getProcesoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(
        summary = "Listar todos los procesos electorales",
        description = "Obtiene una lista completa de todos los procesos electorales registrados"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<ProcesoElectoralResponseDTO>> getAllProcesos() {
        List<ProcesoElectoralResponseDTO> procesos = procesoService.getAllProcesos();
        return ResponseEntity.ok(procesos);
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar un proceso electoral",
        description = "Modifica los datos de un proceso electoral existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proceso electoral actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Proceso electoral no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto con otro proceso (nombre duplicado)")
    })
    public ResponseEntity<ProcesoElectoralResponseDTO> updateProceso(
            @Parameter(description = "ID del proceso electoral a actualizar", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody ProcesoElectoralUpdateRequestDTO requestDTO) {
        ProcesoElectoralResponseDTO response = procesoService.updateProceso(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar un proceso electoral",
        description = "Elimina un proceso electoral del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Proceso electoral eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Proceso electoral no encontrado"),
        @ApiResponse(responseCode = "409", description = "No se puede eliminar porque tiene dependencias")
    })
    public ResponseEntity<Void> deleteProceso(
            @Parameter(description = "ID del proceso electoral a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        procesoService.deleteProceso(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/nombre/{nombre}")
    @Operation(
        summary = "Buscar proceso electoral por nombre",
        description = "Obtiene un proceso electoral específico mediante su nombre exacto"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proceso electoral encontrado"),
        @ApiResponse(responseCode = "404", description = "No existe un proceso con ese nombre")
    })
    public ResponseEntity<ProcesoElectoralResponseDTO> getProcesoByNombre(
            @Parameter(description = "Nombre exacto del proceso electoral", required = true, 
                      example = "Elecciones Generales 2024")
            @PathVariable String nombre) {
        return procesoService.getProcesoByNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/ultimo")
    @Operation(
        summary = "Obtener el último proceso electoral",
        description = "Retorna el proceso electoral más reciente (por fecha de creación o inicio)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Último proceso encontrado"),
        @ApiResponse(responseCode = "404", description = "No hay procesos electorales registrados")
    })
    public ResponseEntity<ProcesoElectoralResponseDTO> getUltimoProceso() {
        return procesoService.getUltimoProceso()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
}