package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.estadoActual.EstadoActualDTO;
import com.credenciales.tribunal.dto.personal.PersonalDTO;
import com.credenciales.tribunal.dto.estadoActual.CambioEstadoResquestDTO;
import com.credenciales.tribunal.model.enums.EstadoPersonal;
import com.credenciales.tribunal.service.EstadoPersonalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/estados-personal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Estados Personal", description = "API para la gestión completa del ciclo de vida de estados del personal electoral")
@SecurityRequirement(name = "bearerAuth")
public class EstadoPersonalController {

    private final EstadoPersonalService estadoPersonalService;

    @Operation(summary = "Registrar nuevo personal", description = "Registra un nuevo personal en el sistema y lo coloca en estado PERSONAL REGISTRADO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Personal registrado exitosamente", content = @Content(schema = @Schema(implementation = PersonalDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "El personal ya existe en el sistema"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/registrar")
    public ResponseEntity<PersonalDTO> registrarPersonal(
            @Valid @RequestBody @Parameter(description = "Datos para el registro del personal", required = true) CambioEstadoResquestDTO request) {
        return new ResponseEntity<>(estadoPersonalService.registrarPersonal(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Imprimir credencial", description = "Cambia el estado del personal a CREDENCIAL IMPRESO. Requiere que el personal esté en estado REGISTRADO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credencial impresa exitosamente"),
            @ApiResponse(responseCode = "400", description = "El personal no está en estado REGISTRADO"),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @PutMapping("/{personalId}/imprimir-credencial")
    public ResponseEntity<PersonalDTO> imprimirCredencial(
            @Parameter(description = "ID del personal", required = true, example = "1") @PathVariable Long personalId) {
        return ResponseEntity.ok(estadoPersonalService.imprimirCredencial(personalId));
    }

    @Operation(summary = "Entregar credencial", description = "Entrega la credencial y automáticamente activa al personal (PERSONAL ACTIVO)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credencial entregada y personal activado"),
            @ApiResponse(responseCode = "400", description = "La credencial debe estar impresa primero"),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @PutMapping("/{personalId}/entregar-credencial")
    public ResponseEntity<PersonalDTO> entregarCredencial(
            @Parameter(description = "ID del personal", required = true, example = "1") @PathVariable Long personalId) {
        return ResponseEntity.ok(estadoPersonalService.entregarCredencial(personalId));
    }

    @Operation(summary = "Habilitar acceso a cómputo", description = "Habilita acceso a cómputo para personal autorizado. Solo si está en estado ACTIVO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acceso a cómputo habilitado"),
            @ApiResponse(responseCode = "400", description = "El personal no tiene acceso a cómputo autorizado o no está ACTIVO"),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @PutMapping("/{personalId}/habilitar-computo")
    public ResponseEntity<PersonalDTO> habilitarAccesoComputo(
            @Parameter(description = "ID del personal", required = true, example = "1") @PathVariable Long personalId) {
        return ResponseEntity.ok(estadoPersonalService.habilitarAccesoComputo(personalId));
    }

    @Operation(summary = "Devolver credencial", description = "Registra la devolución de la credencial y prepara para inactivación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credencial devuelta exitosamente"),
            @ApiResponse(responseCode = "400", description = "El personal no está en estado válido para devolución"),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @PutMapping("/{personalId}/devolver-credencial")
    public ResponseEntity<PersonalDTO> devolverCredencial(
            @Parameter(description = "ID del personal", required = true, example = "1") @PathVariable Long personalId) {
        return ResponseEntity.ok(estadoPersonalService.devolverCredencial(personalId));
    }

    @Operation(summary = "Finalizar proceso electoral", description = "Cambia el estado del personal a INACTIVO por fin de proceso electoral")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proceso finalizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @PutMapping("/{personalId}/finalizar-proceso")
    public ResponseEntity<PersonalDTO> finalizarProcesoElectoral(
            @Parameter(description = "ID del personal", required = true, example = "1") @PathVariable Long personalId) {
        return ResponseEntity.ok(estadoPersonalService.finalizarProcesoElectoral(personalId));
    }

    @Operation(summary = "Registrar renuncia", description = "Registra la renuncia del personal desde cualquier estado válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Renuncia registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede renunciar desde el estado actual"),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @PutMapping("/{personalId}/renunciar")
    public ResponseEntity<PersonalDTO> renunciar(
            @Parameter(description = "ID del personal", required = true, example = "1") @PathVariable Long personalId) {
        return ResponseEntity.ok(estadoPersonalService.renunciar(personalId));
    }

    @Operation(summary = "Obtener personal con estado actual", description = "Obtiene la información completa de un personal incluyendo su estado actual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Personal encontrado"),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @GetMapping("/personal/{personalId}")
    public ResponseEntity<PersonalDTO> obtenerPersonalConEstado(
            @Parameter(description = "ID del personal", required = true, example = "1") @PathVariable Long personalId) {
        return ResponseEntity.ok(estadoPersonalService.obtenerPersonalConEstadoActual(personalId));
    }

    @Operation(summary = "Listar personal por estado", description = "Obtiene todos los personales que se encuentran en un estado específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Estado no válido")
    })
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PersonalDTO>> listarPersonalPorEstado(
            @Parameter(description = "Estado del personal", required = true, schema = @Schema(implementation = EstadoPersonal.class)) @PathVariable EstadoPersonal estado) {
        return ResponseEntity.ok(estadoPersonalService.listarPersonalPorEstado(estado));
    }

    @Operation(summary = "Obtener historial de estados", description = "Obtiene todo el historial de estados por los que ha pasado un personal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @GetMapping("/personal/{personalId}/historial")
    public ResponseEntity<List<EstadoActualDTO>> obtenerHistorialEstados(
            @Parameter(description = "ID del personal", required = true, example = "1") @PathVariable Long personalId) {
        return ResponseEntity.ok(estadoPersonalService.obtenerHistorialEstados(personalId));
    }

    @Operation(summary = "Obtener estados permitidos", description = "Obtiene los estados a los que puede transitar el personal desde su estado actual")
    @GetMapping("/personal/{personalId}/estados-permitidos")
    public ResponseEntity<List<EstadoPersonal>> obtenerEstadosPermitidos(
            @Parameter(description = "ID del personal", required = true, example = "1") @PathVariable Long personalId) {
        return ResponseEntity.ok(estadoPersonalService.obtenerEstadosPermitidos(personalId));
    }

    @Operation(summary = "Verificar si puede habilitar cómputo", description = "Verifica si el personal cumple los requisitos para habilitar acceso a cómputo")
    @GetMapping("/personal/{personalId}/puede-habilitar-computo")
    public ResponseEntity<Boolean> puedeHabilitarAccesoComputo(
            @Parameter(description = "ID del personal", required = true, example = "1") @PathVariable Long personalId) {
        return ResponseEntity.ok(estadoPersonalService.puedeHabilitarseAccesoComputo(personalId));
    }

    @PutMapping("/{personalId}/estado-registrado")
    public ResponseEntity<PersonalDTO> estadoRegistrado(@PathVariable Long personalId) {
        PersonalDTO personalDTO = estadoPersonalService.estadoRegistrado(personalId);
        return ResponseEntity.ok(personalDTO);
    }

}