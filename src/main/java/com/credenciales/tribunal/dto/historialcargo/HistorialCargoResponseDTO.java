package com.credenciales.tribunal.dto.historialcargo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCargoResponseDTO {
    private Long id;
    private Long personalId;
    private String personalNombre;
    private String personalApellido;
    private String personalDocumento;
    private Long cargoId;
    private String cargoNombre;
    private Long unidadId;
    private String unidadNombre;
    private String unidadAbreviatura;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean activo;
    private Integer duracionDias; // Cálculo opcional
}