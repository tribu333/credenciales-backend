package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.qr.QrAsignacionDTO;
import com.credenciales.tribunal.dto.qr.QrGenerarDTO;
import com.credenciales.tribunal.dto.qr.QrResponseDTO;
import com.credenciales.tribunal.model.entity.Qr;
import com.credenciales.tribunal.model.enums.TipoQr;
import com.credenciales.tribunal.service.QrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/qr")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "QR", description = "API para la gestión de códigos QR del personal")
public class QrController {

    private final QrService qrService;

    @Operation(
        summary = "Generar QR para personal",
        description = "Genera un nuevo código QR para un personal basado en su carnet de identidad"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "QR generado exitosamente",
                    content = @Content(schema = @Schema(implementation = QrResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Ya existe un QR para este carnet")
    })
    @PostMapping("/generar/personal")
    public ResponseEntity<QrResponseDTO> generarQrPersonal(
            @Valid @RequestBody 
            @Parameter(description = "Datos para generar QR", required = true)
            QrGenerarDTO qrGenerarDTO) {
        return new ResponseEntity<>(qrService.generarQrPersonal(qrGenerarDTO), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Generar QR para externo",
        description = "Genera un nuevo código QR para uso externo"
    )
    @PostMapping("/generar/externo")
    public ResponseEntity<QrResponseDTO> generarQrExterno(
            @Valid @RequestBody QrGenerarDTO qrGenerarDTO) {
        return new ResponseEntity<>(qrService.generarQrExterno(qrGenerarDTO), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Obtener QR por ID",
        description = "Obtiene la información de un QR por su ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<QrResponseDTO> obtenerQrPorId(
            @Parameter(description = "ID del QR", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(qrService.obtenerQrPorId(id));
    }

    @Operation(
        summary = "Obtener QR por código",
        description = "Obtiene la información de un QR por su código único"
    )
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<QrResponseDTO> obtenerQrPorCodigo(
            @Parameter(description = "Código del QR", required = true, example = "QR-1234567-20260215-ABC123")
            @PathVariable String codigo) {
        return ResponseEntity.ok(qrService.obtenerQrPorCodigo(codigo));
    }

    @Operation(
        summary = "Obtener QR por personal",
        description = "Obtiene el QR asignado a un personal específico"
    )
    @GetMapping("/personal/{personalId}")
    public ResponseEntity<QrResponseDTO> obtenerQrPorPersonalId(
            @Parameter(description = "ID del personal", required = true, example = "1")
            @PathVariable Long personalId) {
        return ResponseEntity.ok(qrService.obtenerQrPorPersonalId(personalId));
    }

    @Operation(
        summary = "Listar QR libres",
        description = "Obtiene todos los QR disponibles (estado LIBRE)"
    )
    @GetMapping("/libres")
    public ResponseEntity<List<QrResponseDTO>> listarQrsLibres() {
        return ResponseEntity.ok(qrService.listarQrsLibres());
    }

    @Operation(
        summary = "Listar QR por tipo",
        description = "Obtiene todos los QR libres de un tipo específico"
    )
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<QrResponseDTO>> listarQrsPorTipo(
            @Parameter(description = "Tipo de QR", required = true, example = "PERSONAL")
            @PathVariable TipoQr tipo) {
        return ResponseEntity.ok(qrService.listarQrsPorTipo(tipo));
    }

    @Operation(
        summary = "Asignar QR a personal",
        description = "Asigna un QR existente a un personal"
    )
    @PutMapping("/{qrId}/asignar/{personalId}")
    public ResponseEntity<QrResponseDTO> asignarQrAPersonal(
            @Parameter(description = "ID del QR", required = true, example = "1")
            @PathVariable Long qrId,
            @Parameter(description = "ID del personal", required = true, example = "1")
            @PathVariable Long personalId) {
        Qr qr = qrService.asignarQrAPersonal(qrId, personalId);
        return ResponseEntity.ok(qrService.obtenerQrPorId(qr.getId()));
    }

    @Operation(
        summary = "Liberar QR",
        description = "Libera un QR asignado, dejándolo disponible para otro personal"
    )
    @PutMapping("/{qrId}/liberar")
    public ResponseEntity<QrResponseDTO> liberarQr(
            @Parameter(description = "ID del QR", required = true, example = "1")
            @PathVariable Long qrId) {
        Qr qr = qrService.liberarQr(qrId);
        return ResponseEntity.ok(qrService.obtenerQrPorId(qr.getId()));
    }

    @Operation(
        summary = "Inactivar QR",
        description = "Inactiva un QR, dejándolo fuera de uso"
    )
    @PutMapping("/{qrId}/inactivar")
    public ResponseEntity<QrResponseDTO> inactivarQr(
            @Parameter(description = "ID del QR", required = true, example = "1")
            @PathVariable Long qrId) {
        Qr qr = qrService.inactivarQr(qrId);
        return ResponseEntity.ok(qrService.obtenerQrPorId(qr.getId()));
    }

    @Operation(
        summary = "Descargar imagen QR",
        description = "Descarga la imagen del QR en formato PNG"
    )
    @GetMapping("/{qrId}/imagen")
    public ResponseEntity<byte[]> descargarImagenQr(
            @Parameter(description = "ID del QR", required = true, example = "1")
            @PathVariable Long qrId) {
        byte[] imagen = qrService.descargarImagenQr(qrId);
        
        QrResponseDTO qrInfo = qrService.obtenerQrPorId(qrId);
        String filename = qrInfo.getCodigo() + ".png";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", filename);
        
        return new ResponseEntity<>(imagen, headers, HttpStatus.OK);
    }

    @Operation(
        summary = "Ver imagen QR",
        description = "Visualiza la imagen del QR en el navegador"
    )
    @GetMapping("/{qrId}/ver")
    public ResponseEntity<byte[]> verImagenQr(
            @Parameter(description = "ID del QR", required = true, example = "1")
            @PathVariable Long qrId) {
        byte[] imagen = qrService.descargarImagenQr(qrId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        
        return new ResponseEntity<>(imagen, headers, HttpStatus.OK);
    }
}