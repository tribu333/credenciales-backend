package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.contrato.ContratoCreateRequestDTO;
import com.credenciales.tribunal.dto.contrato.ContratoUpdateRequestDTO;
import com.credenciales.tribunal.dto.contrato.ContratoResponseDTO;
import com.credenciales.tribunal.service.ContratoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Contratos",
     description = "API para gestionar contratos del personal")
public class ContratoController {

    private final ContratoService contratoService;

    @PostMapping
    @Operation(
        summary = "Crear un nuevo contrato",
        description = "Registra un nuevo contrato para un miembro del personal"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Contrato creado exitosamente",
                content = @Content(schema = @Schema(implementation = ContratoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Personal, cargo o proceso no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto en la creación del contrato")
    })
    public ResponseEntity<ContratoResponseDTO> createContrato(
            @Valid @RequestBody ContratoCreateRequestDTO requestDTO) {

        ContratoResponseDTO response = contratoService.createContrato(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener contrato por ID",
        description = "Retorna los detalles de un contrato específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrato encontrado"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    public ResponseEntity<ContratoResponseDTO> getContratoById(
            @Parameter(description = "ID del contrato", required = true, example = "1")
            @PathVariable Long id) {

        return contratoService.getContratoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
        summary = "Listar todos los contratos",
        description = "Obtiene una lista completa de contratos"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<ContratoResponseDTO>> getAllContratos() {

        List<ContratoResponseDTO> contratos = contratoService.getAllContratos();
        return ResponseEntity.ok(contratos);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar contrato",
        description = "Modifica los datos de un contrato existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrato actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    public ResponseEntity<ContratoResponseDTO> updateContrato(
            @Parameter(description = "ID del contrato a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ContratoUpdateRequestDTO requestDTO) {

        ContratoResponseDTO response = contratoService.updateContrato(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar contrato",
        description = "Elimina lógicamente un contrato"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Contrato eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    public ResponseEntity<Void> deleteContrato(
            @Parameter(description = "ID del contrato a eliminar", required = true, example = "1")
            @PathVariable Long id) {

        contratoService.deleteContrato(id);
        return ResponseEntity.noContent().build();
    }
}