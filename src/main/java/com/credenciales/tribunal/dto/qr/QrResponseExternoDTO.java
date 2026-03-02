package com.credenciales.tribunal.dto.qr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.credenciales.tribunal.model.enums.EstadoQr;
import com.credenciales.tribunal.model.enums.TipoQr;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO con la información del QR generado")
public class QrResponseExternoDTO {
        
        @Schema(description = "ID del QR", example = "1")
        private Long id;

        @Schema(description = "Código único del QR", example = "QR-1234567-20260215-ABC123")
        private String codigo;

        @Schema(description = "Tipo de QR", example = "PERSONAL")
        private TipoQr tipo;

        @Schema(description = "Tipo de esterno QR", example = "PERSONAL")
        private TipoQr tipo_externo;

        @Schema(description = "Estado del QR", example = "LIBRE")
        private EstadoQr estado;

        @Schema(description = "Fecha de creación", example = "2026-02-15T10:30:00")
        private LocalDateTime createdAt;
}