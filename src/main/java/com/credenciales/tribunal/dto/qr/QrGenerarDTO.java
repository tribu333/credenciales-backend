package com.credenciales.tribunal.dto.qr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class QrGenerarDTO {
    
    @NotBlank(message = "El código del personal es requerido")
    @Size(min = 5, max = 20, message = "El carnet de identidad debe tener entre 5 y 20 caracteres")
    @Pattern(regexp = "^[0-9]+[A-Za-z]?$", message = "El carnet debe contener solo números y opcionalmente una letra")
    @Schema(description = "Carnet de identidad del personal", example = "1234567", required = true)
    private String carnetIdentidad;
    
    @NotNull(message = "El tipo de QR es requerido")
    @Schema(description = "Tipo de QR", example = "PERSONAL", allowableValues = {"PERSONAL", "EXTERNO"})
    private TipoQr tipo;
}
