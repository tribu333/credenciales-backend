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
    private String cargoProcesoDescripcion;
    private Boolean cargoProcesoActivo;
    
    // Información del proceso
    private Long procesoId;
    private String procesoNombre;
    private LocalDateTime procesoFechaInicio;
    private LocalDateTime procesoFechaFin;
    private Boolean procesoActivo;
    
    // Información de la unidad
    private Long unidadId;
    private String unidadNombre;
    private String unidadAbreviatura;
    
    // Información del personal
    private Long personalId;
    private String personalNombre;
    private String personalApellido;
    private String personalDocumento;
    private String personalEmail;
    
    // Información del historial
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean activo;
    private Long duracionHoras;
    private Long duracionDias;
    private String estado; // "ACTIVO", "FINALIZADO", "CANCELADO"
}
