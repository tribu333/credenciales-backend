package com.credenciales.tribunal.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para solicitar código de verificación")
public class VerificacionEmailRequestDTO {
    
    @Email(message = "Formato de correo inválido")
    @Size(max = 150, message = "El correo no puede exceder los 150 caracteres")
    @Schema(description = "Correo electrónico", example = "juan.perez@ejemplo.com", required = true)
    private String correo;
    
    @NotBlank(message = "El carnet de identidad es requerido")
    @Size(min = 5, max = 20, message = "El carnet debe tener entre 5 y 20 caracteres")
    @Pattern(regexp = "^[0-9]+[A-Za-z]?$", message = "Formato de carnet inválido")
    @Schema(description = "Carnet de identidad", example = "1234567", required = true)
    private String carnetIdentidad;
}
