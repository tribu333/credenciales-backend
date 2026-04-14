package com.credenciales.tribunal.dto.historialcargoproceso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCargoProcesoResponseDTO {
    private Long id;
    
    // Información del cargo proceso
    private Long cargoProcesoId;
    private String cargoProcesoNombre;
    
    // Información de la unidad
    private Long unidadId;
    private String unidadNombre;
    
    // Información del personal
    private Long personalId;
    private String personalNombre;
    private String personalApellido;
    private String personalApellidoM;
    
    // Información del historial
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean activo;
    private String tipoContrato;
}
