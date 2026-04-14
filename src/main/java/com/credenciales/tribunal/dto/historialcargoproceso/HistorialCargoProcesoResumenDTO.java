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
public class HistorialCargoProcesoResumenDTO {
    private Long id;
    private String cargoProcesoNombre;
    private Long personalId;
    private String personalNombre;
    private String personalApellidoPaterno;
    private String personalApellidoMaterno;
    private String personalCarnet;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean activo;
    private String tipoContrato;
}
