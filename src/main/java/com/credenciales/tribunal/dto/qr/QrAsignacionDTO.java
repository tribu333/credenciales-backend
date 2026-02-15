package com.credenciales.tribunal.dto.qr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para asignar un QR a un personal")
public class QrAsignacionDTO {
    
    @Schema(description = "ID del QR", example = "1")
    private Long qrId;
    
    @Schema(description = "ID del personal", example = "1")
    private Long personalId;
    
    @Schema(description = "Código del QR", example = "QR-1234567-20260215-ABC123")
    private String codigoQr;
    
    @Schema(description = "Nombre del personal", example = "Juan Carlos Pérez")
    private String personalNombre;
    
}
