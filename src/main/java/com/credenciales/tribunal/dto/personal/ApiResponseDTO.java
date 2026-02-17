package com.credenciales.tribunal.dto.personal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta genérica de la API")
public class ApiResponseDTO {
    
    @Schema(description = "Indica si la operación fue exitosa", example = "true")
    private boolean success;
    
    @Schema(description = "Mensaje descriptivo", example = "Operación completada exitosamente")
    private String message;
    
    @Schema(description = "Código de respuesta", example = "200")
    private int status;
    
    @Schema(description = "Timestamp de la respuesta")
    private String timestamp;
}
