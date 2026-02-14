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
public class HistorialCargoProcesoDTO {
    private Long id;
    private Long cargoProcesoId;
    private String cargoProcesoNombre;
    private Long procesoId;
    private String procesoNombre;
    private Long unidadId;
    private String unidadNombre;
    private Long personalId;
    private String personalNombre;
    private String personalApellido;
    private String personalDocumento;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean activo;
    private Integer duracionDias;
}
