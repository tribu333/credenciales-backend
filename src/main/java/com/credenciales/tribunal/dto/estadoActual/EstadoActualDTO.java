package com.credenciales.tribunal.dto.estadoActual;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO del historial de estados")
public class EstadoActualDTO {
    
    @Schema(description = "ID del registro de estado", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(description = "ID del personal", example = "1")
    private Long personalId;
    
    @Schema(description = "Nombre completo del personal", example = "Juan Carlos Pérez González")
    private String personalNombre;
    
    @Schema(description = "Nombre del estado", 
            example = "PERSONAL ACTIVO", 
            allowableValues = {"PERSONAL REGISTRADO", "CREDENCIAL IMPRESO", "CREDENCIAL ENTREGADO", 
                            "PERSONAL ACTIVO", "PERSONAL CON ACCESO A COMPUTO", "CREDENCIAL DEVUELTO", 
                            "INACTIVO PROCESO ELECTORAL TERMINADO", "INACTIVO POR RENUNCIA"})
    private String estadoNombre;
    
    @Schema(description = "Indica si es el estado actual", example = "true")
    private Boolean valorEstadoActual;
    
    @Schema(description = "Fecha y hora del cambio de estado", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}
