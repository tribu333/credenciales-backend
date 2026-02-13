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
public class HistorialCargoDTO {
    private Long id;
    private Long personalId;
    private String personalNombre;
    private String personalApellido;
    private Long cargoId;
    private String cargoNombre;
    private String unidadNombre;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean activo;
}