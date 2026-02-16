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
public class QrResponseDTO {
        
        @Schema(description = "ID del QR", example = "1")
        private Long id;

        @Schema(description = "Código único del QR", example = "QR-1234567-20260215-ABC123")
        private String codigo;

        @Schema(description = "Tipo de QR", example = "PERSONAL")
        private TipoQr tipo;

        @Schema(description = "Estado del QR", example = "LIBRE")
        private EstadoQr estado;

        @Schema(description = "Ruta de la imagen QR en el servidor", 
                example = "/uploads/qr/qr-1234567-20260215.png")
        private String rutaImagenQr;

        @Schema(description = "URL pública para acceder al QR", 
                example = "http://localhost:8080/uploads/qr/qr-1234567-20260215.png")
        private String urlPublica;

        @Schema(description = "Fecha de creación", example = "2026-02-15T10:30:00")
        private LocalDateTime createdAt;
}