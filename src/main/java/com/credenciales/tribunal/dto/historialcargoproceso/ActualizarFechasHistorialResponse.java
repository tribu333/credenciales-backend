package com.credenciales.tribunal.dto.historialcargoproceso;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarFechasHistorialResponse {
    private Long procesoElectoralId;
    private String nombreProceso;
    private Long cargoProcesoId;
    private String nombreCargo;
    private Integer historialesActualizados;
    private LocalDateTime nuevaFechaInicio;
    private LocalDateTime nuevaFechaFin;
}