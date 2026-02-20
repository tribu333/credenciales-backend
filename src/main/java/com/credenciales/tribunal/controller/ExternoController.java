package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.externo.ExternoDTO;
import com.credenciales.tribunal.dto.externo.ExternoDetalleResponseDTO;
import com.credenciales.tribunal.dto.externo.ExternoRequestDTO;
import com.credenciales.tribunal.dto.externo.ExternoResponseDTO;
import com.credenciales.tribunal.model.enums.TipoExterno;
import com.credenciales.tribunal.service.ExternoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/externos")
@RequiredArgsConstructor
@Tag(name = "Externos", description = "API para la gestión de personas externas (periodistas, invitados, etc.)")
public class ExternoController {

    private final ExternoService externoService;

    @PostMapping
    @Operation(
        summary = "Crear un nuevo externo",
        description = "Registra una nueva persona externa en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Externo creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ExternoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o carnet duplicado"),
        @ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    })
    public ResponseEntity<ExternoResponseDTO> crearExterno(
            @Valid @RequestBody 
            @Parameter(description = "Datos del externo a crear", required = true) 
            ExternoRequestDTO requestDTO) {
        return new ResponseEntity<>(externoService.crearExterno(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar un externo existente",
        description = "Actualiza los datos de una persona externa por su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Externo actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ExternoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Externo o imagen no encontrada")
    })
    public ResponseEntity<ExternoResponseDTO> actualizarExterno(
            @PathVariable 
            @Parameter(description = "ID del externo a actualizar", example = "1", required = true) 
            Long id,
            
            @Valid @RequestBody 
            @Parameter(description = "Datos actualizados del externo", required = true) 
            ExternoRequestDTO requestDTO) {
        return ResponseEntity.ok(externoService.actualizarExterno(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar un externo",
        description = "Elimina lógicamente una persona externa del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Externo eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Externo no encontrado")
    })
    public ResponseEntity<Void> eliminarExterno(
            @PathVariable 
            @Parameter(description = "ID del externo a eliminar", example = "1", required = true) 
            Long id) {
        externoService.eliminarExterno(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener externo por ID",
        description = "Retorna los datos básicos de una persona externa por su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Externo encontrado",
                    content = @Content(schema = @Schema(implementation = ExternoDTO.class))),
        @ApiResponse(responseCode = "404", description = "Externo no encontrado")
    })
    public ResponseEntity<ExternoDTO> obtenerExterno(
            @PathVariable 
            @Parameter(description = "ID del externo", example = "1", required = true) 
            Long id) {
        return ResponseEntity.ok(externoService.obtenerExternoPorId(id));
    }

    @GetMapping("/{id}/detalle")
    @Operation(
        summary = "Obtener detalle completo de externo",
        description = "Retorna los datos completos de un externo incluyendo imagen y asignaciones QR"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle de externo encontrado",
                    content = @Content(schema = @Schema(implementation = ExternoDetalleResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Externo no encontrado")
    })
    public ResponseEntity<ExternoDetalleResponseDTO> obtenerDetalleExterno(
            @PathVariable 
            @Parameter(description = "ID del externo", example = "1", required = true) 
            Long id) {
        return ResponseEntity.ok(externoService.obtenerExternoDetallePorId(id));
    }

    @GetMapping
    @Operation(
        summary = "Listar todos los externos",
        description = "Retorna una lista básica de todos los externos registrados"
    )
    @ApiResponse(responseCode = "200", description = "Lista de externos obtenida exitosamente")
    public ResponseEntity<List<ExternoDTO>> listarTodos() {
        return ResponseEntity.ok(externoService.listarTodos());
    }

    @GetMapping("/response")
    @Operation(
        summary = "Listar externos con información de imagen",
        description = "Retorna una lista de externos incluyendo información básica de la imagen asociada"
    )
    @ApiResponse(responseCode = "200", description = "Lista de externos obtenida exitosamente")
    public ResponseEntity<List<ExternoResponseDTO>> listarTodosResponse() {
        return ResponseEntity.ok(externoService.listarTodosResponse());
    }

    @GetMapping("/detalles")
    @Operation(
        summary = "Listar externos con detalles completos",
        description = "Retorna una lista detallada de externos incluyendo imagen y asignaciones QR"
    )
    @ApiResponse(responseCode = "200", description = "Lista detallada de externos obtenida exitosamente")
    public ResponseEntity<List<ExternoDetalleResponseDTO>> listarTodosDetalles() {
        return ResponseEntity.ok(externoService.listarTodosDetalles());
    }

    @GetMapping("/carnet/{carnetIdentidad}")
    @Operation(
        summary = "Buscar externo por carnet de identidad",
        description = "Retorna un externo por su número de carnet de identidad (único)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Externo encontrado",
                    content = @Content(schema = @Schema(implementation = ExternoDTO.class))),
        @ApiResponse(responseCode = "404", description = "Externo no encontrado con ese carnet")
    })
    public ResponseEntity<ExternoDTO> obtenerPorCarnet(
            @PathVariable 
            @Parameter(description = "Número de carnet de identidad", example = "1234567", required = true) 
            String carnetIdentidad) {
        return ResponseEntity.ok(externoService.obtenerPorCarnetIdentidad(carnetIdentidad));
    }

    @GetMapping("/identificador/{identificador}")
    @Operation(
        summary = "Listar externos por identificador exacto",
        description = "Retorna todos los externos que pertenecen a una organización específica (ej: TVU, Los Tiempos)"
    )
    @ApiResponse(responseCode = "200", description = "Lista de externos de la organización")
    public ResponseEntity<List<ExternoDTO>> listarPorIdentificador(
            @PathVariable 
            @Parameter(description = "Nombre de la organización/identificador", example = "TVU", required = true) 
            String identificador) {
        return ResponseEntity.ok(externoService.listarPorIdentificador(identificador));
    }

    @GetMapping("/identificador/buscar")
    @Operation(
        summary = "Buscar externos por identificador parcial",
        description = "Retorna externos cuyo identificador contenga el texto proporcionado (búsqueda parcial)"
    )
    @ApiResponse(responseCode = "200", description = "Lista de externos que coinciden con la búsqueda")
    public ResponseEntity<List<ExternoDTO>> listarPorIdentificadorParcial(
            @RequestParam 
            @Parameter(description = "Texto a buscar en el identificador", example = "TV", required = true) 
            String texto) {
        return ResponseEntity.ok(externoService.listarPorIdentificadorParcial(texto));
    }

    @GetMapping("/tipo/{tipoExterno}")
    @Operation(
        summary = "Listar externos por tipo",
        description = "Retorna todos los externos de un tipo específico (PERIODISTA, INVITADO, etc.)"
    )
    @ApiResponse(responseCode = "200", description = "Lista de externos del tipo especificado")
    public ResponseEntity<List<ExternoDTO>> listarPorTipo(
            @PathVariable 
            @Parameter(description = "Tipo de externo", example = "PERIODISTA", required = true) 
            TipoExterno tipoExterno) {
        return ResponseEntity.ok(externoService.listarPorTipoExterno(tipoExterno));
    }

    @GetMapping("/organizacion")
    @Operation(
        summary = "Buscar externos por organización política",
        description = "Retorna externos cuya organización política contenga el texto proporcionado"
    )
    @ApiResponse(responseCode = "200", description = "Lista de externos que coinciden con la organización política")
    public ResponseEntity<List<ExternoDTO>> listarPorOrganizacion(
            @RequestParam 
            @Parameter(description = "Nombre de la organización política", example = "MAS", required = true) 
            String nombre) {
        return ResponseEntity.ok(externoService.listarPorOrganizacionPolitica(nombre));
    }

    @GetMapping("/existe/carnet")
    @Operation(
        summary = "Verificar si existe un carnet de identidad",
        description = "Retorna true si ya existe un externo con el carnet proporcionado"
    )
    @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    public ResponseEntity<Boolean> existePorCarnet(
            @RequestParam 
            @Parameter(description = "Número de carnet a verificar", example = "1234567", required = true) 
            String carnetIdentidad) {
        return ResponseEntity.ok(externoService.existePorCarnetIdentidad(carnetIdentidad));
    }

    @GetMapping("/existe/identificador")
    @Operation(
        summary = "Verificar si existe algún externo con un identificador",
        description = "Retorna true si existe al menos un externo con el identificador proporcionado"
    )
    @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    public ResponseEntity<Boolean> existeAlgunoPorIdentificador(
            @RequestParam 
            @Parameter(description = "Identificador a verificar", example = "TVU", required = true) 
            String identificador) {
        return ResponseEntity.ok(externoService.existeAlgunoPorIdentificador(identificador));
    }

    @GetMapping("/count")
    @Operation(
        summary = "Contar total de externos",
        description = "Retorna el número total de externos registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Total de externos")
    public ResponseEntity<Long> contarTotal() {
        return ResponseEntity.ok(externoService.contarTotal());
    }

    @GetMapping("/count/tipo/{tipoExterno}")
    @Operation(
        summary = "Contar externos por tipo",
        description = "Retorna el número de externos de un tipo específico"
    )
    @ApiResponse(responseCode = "200", description = "Cantidad de externos del tipo especificado")
    public ResponseEntity<Long> contarPorTipo(
            @PathVariable 
            @Parameter(description = "Tipo de externo", example = "PERIODISTA", required = true) 
            TipoExterno tipoExterno) {
        return ResponseEntity.ok(externoService.contarPorTipoExterno(tipoExterno));
    }

    @GetMapping("/count/identificador/{identificador}")
    @Operation(
        summary = "Contar externos por identificador",
        description = "Retorna el número de externos que pertenecen a una organización específica"
    )
    @ApiResponse(responseCode = "200", description = "Cantidad de externos de la organización")
    public ResponseEntity<Long> contarPorIdentificador(
            @PathVariable 
            @Parameter(description = "Nombre de la organización", example = "TVU", required = true) 
            String identificador) {
        return ResponseEntity.ok(externoService.contarPorIdentificador(identificador));
    }
}