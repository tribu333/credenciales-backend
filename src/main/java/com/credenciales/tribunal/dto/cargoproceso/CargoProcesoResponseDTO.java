package com.credenciales.tribunal.dto.cargoproceso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoProcesoResponseDTO {
    private Long id;
    private Long procesoId;
    private String procesoNombre;
    private LocalDateTime procesoFechaInicio;
    private LocalDateTime procesoFechaFin;
    private Boolean procesoActivo;
    private String nombre;
    private Long unidadId;
    private String unidadNombre;
    private String unidadAbreviatura;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime createdAt;
    private Integer totalHistoriales;
    private List<HistorialResumenDTO> ultimosHistoriales;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class HistorialResumenDTO {
    private Long id;
    private String personalNombre;
    private String personalApellido;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean activo;
}