package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.acceso.AccesoDTO;
import com.credenciales.tribunal.dto.acceso.AccesoResponseDTO;
import com.credenciales.tribunal.dto.acceso.AsignacionQrInfoDTO;
import com.credenciales.tribunal.dto.acceso.QrInfoDTO;
import com.credenciales.tribunal.model.entity.Acceso;
import com.credenciales.tribunal.model.enums.TipoEventoAcceso;
import com.credenciales.tribunal.service.AccesoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accesos")
@RequiredArgsConstructor
@Tag(name = "Accesos", description = "API para gestión de accesos (entradas y salidas)")
@CrossOrigin(origins = "*")
public class AccesoController {

    private final AccesoService accesoService;

    @PostMapping
    @Operation(summary = "Registrar un nuevo acceso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Acceso registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "QR o Asignación no encontrados")
    })
    public ResponseEntity<AccesoResponseDTO> registrarAcceso(@Valid @RequestBody AccesoDTO accesoDTO) {
        Acceso acceso = accesoService.registrarAcceso(accesoDTO);
        return new ResponseEntity<>(convertToResponseDTO(acceso), HttpStatus.CREATED);
    }

    @PostMapping("/entrada")
    @Operation(summary = "Registrar una entrada")
    public ResponseEntity<AccesoResponseDTO> registrarEntrada(
            @RequestParam(required = false) Long qrId,
            @RequestParam(required = false) Long asignacionQrId) {
        Acceso acceso = accesoService.registrarEntrada(qrId, asignacionQrId);
        return new ResponseEntity<>(convertToResponseDTO(acceso), HttpStatus.CREATED);
    }

    @PostMapping("/salida")
    @Operation(summary = "Registrar una salida")
    public ResponseEntity<AccesoResponseDTO> registrarSalida(
            @RequestParam(required = false) Long qrId,
            @RequestParam(required = false) Long asignacionQrId) {
        Acceso acceso = accesoService.registrarSalida(qrId, asignacionQrId);
        return new ResponseEntity<>(convertToResponseDTO(acceso), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los accesos")
    public ResponseEntity<List<AccesoResponseDTO>> obtenerTodosLosAccesos() {
        List<AccesoResponseDTO> accesos = accesoService.obtenerTodosLosAccesos()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accesos);
    }

    @GetMapping("/paginados")
    @Operation(summary = "Obtener accesos paginados")
    public ResponseEntity<Page<AccesoResponseDTO>> obtenerAccesosPaginados(
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AccesoResponseDTO> accesos = accesoService.obtenerAccesosPaginados(pageable)
                .map(this::convertToResponseDTO);
        return ResponseEntity.ok(accesos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un acceso por ID")
    public ResponseEntity<AccesoResponseDTO> obtenerAccesoPorId(@PathVariable Long id) {
        Acceso acceso = accesoService.obtenerAccesoPorId(id);
        return ResponseEntity.ok(convertToResponseDTO(acceso));
    }

    @GetMapping("/qr/{qrId}")
    @Operation(summary = "Obtener accesos por QR ID")
    public ResponseEntity<List<AccesoResponseDTO>> obtenerAccesosPorQr(@PathVariable Long qrId) {
        List<AccesoResponseDTO> accesos = accesoService.obtenerAccesosPorQr(qrId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accesos);
    }

    @GetMapping("/asignacion/{asignacionQrId}")
    @Operation(summary = "Obtener accesos por Asignación QR ID")
    public ResponseEntity<List<AccesoResponseDTO>> obtenerAccesosPorAsignacionQr(
            @PathVariable Long asignacionQrId) {
        List<AccesoResponseDTO> accesos = accesoService.obtenerAccesosPorAsignacionQr(asignacionQrId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accesos);
    }

    @GetMapping("/tipo/{tipoEvento}")
    @Operation(summary = "Obtener accesos por tipo de evento")
    public ResponseEntity<List<AccesoResponseDTO>> obtenerAccesosPorTipoEvento(
            @PathVariable TipoEventoAcceso tipoEvento) {
        List<AccesoResponseDTO> accesos = accesoService.obtenerAccesosPorTipoEvento(tipoEvento)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accesos);
    }

    @GetMapping("/rango-fechas")
    @Operation(summary = "Obtener accesos en un rango de fechas")
    public ResponseEntity<List<AccesoResponseDTO>> obtenerAccesosPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<AccesoResponseDTO> accesos = accesoService.obtenerAccesosPorRangoFechas(inicio, fin)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(accesos);
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas de accesos")
    public ResponseEntity<AccesoService.EstadisticasAcceso> obtenerEstadisticas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(accesoService.obtenerEstadisticas(inicio, fin));
    }

//    @GetMapping("/qr/{qrId}/activo")
//    @Operation(summary = "Verificar si un QR tiene acceso activo")
//    public ResponseEntity<Boolean> tieneAccesoActivo(@PathVariable Long qrId) {
//        return ResponseEntity.ok(accesoService.tieneAccesoActivo(qrId));
//    }

    @GetMapping("/qr/{qrId}/ultimo")
    @Operation(summary = "Obtener el último acceso de un QR")
    public ResponseEntity<AccesoResponseDTO> obtenerUltimoAccesoQr(@PathVariable Long qrId) {
        Acceso acceso = accesoService.obtenerUltimoAccesoQr(qrId);
        return acceso != null ? ResponseEntity.ok(convertToResponseDTO(acceso)) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un acceso (solo administrativo)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Acceso eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Acceso no encontrado")
    })
    public ResponseEntity<Void> eliminarAcceso(@PathVariable Long id) {
        accesoService.eliminarAcceso(id);
        return ResponseEntity.noContent().build();
    }

    private AccesoResponseDTO convertToResponseDTO(Acceso acceso) {
        QrInfoDTO qrInfoDTO = null;
        if (acceso.getQr() != null) {
            qrInfoDTO = QrInfoDTO.builder()
                    .id(acceso.getQr().getId())
                    .codigo(acceso.getQr().getCodigo())
                    // Si no tienes getActivo(), comenta esta línea
                    // .activo(acceso.getQr().getActivo())
                    .build();
        }

        AsignacionQrInfoDTO asignacionQrInfoDTO = null;
        if (acceso.getAsignacionQr() != null) {
            asignacionQrInfoDTO = AsignacionQrInfoDTO.builder()
                    .id(acceso.getAsignacionQr().getId())
                    // Si no tienes getDescripcion(), usa otro método o comenta
                    // .descripcion(acceso.getAsignacionQr().getDescripcion())
                    .build();
        }

        return AccesoResponseDTO.builder()
                .id(acceso.getId())
                .fechaHora(acceso.getFechaHora())
                .tipoEvento(acceso.getTipoEvento())
                .qr(qrInfoDTO)
                .asignacionQr(asignacionQrInfoDTO)
                .build();
    }
}