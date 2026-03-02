package com.credenciales.tribunal.dto.qr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.credenciales.tribunal.model.enums.TipoQr;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para generar un nuevo código QR")
public class QrGenerarExternoDTO {
    
    @NotBlank(message = "El tipo de externo del personal es requerido")
    @Schema(description = "Tipo de externo (PRENSA, DELEGADO, CANDIDATO, OBSERVADOR, PUBLICO_GENERAL)", example = "PRENSA", required = true)
    private String tipo_externo;

    @NotNull(message = "El tipo de QR es requerido")
    @Schema(description = "Tipo de QR", example = "PERSONAL", allowableValues = {"PERSONAL", "EXTERNO"})
    private TipoQr tipo;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
}
