package com.credenciales.tribunal.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de verificación de email")
public class VerificacionResponseDTO {
    
    @Schema(description = "Mensaje descriptivo", 
            example = "Código de verificación enviado a juan@ejemplo.com")
    private String mensaje;
    
    @Schema(description = "Email al que se envió el código", 
            example = "juan@ejemplo.com")
    private String email;
    
    @Schema(description = "Tiempo de expiración en minutos", example = "5")
    private int expiracionMinutos;
    
    @Schema(description = "Timestamp del envío")
    private String timestamp;
}
