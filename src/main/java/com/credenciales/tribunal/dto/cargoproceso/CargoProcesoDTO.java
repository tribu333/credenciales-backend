package com.credenciales.tribunal.dto.cargoproceso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoProcesoDTO {
    private Long id;
    private Long procesoId;
    private String procesoNombre;
    private String nombre;
    private Long unidadId;
    private String unidadNombre;
    private String unidadAbreviatura;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime createdAt;
    private Integer totalHistoriales;
}