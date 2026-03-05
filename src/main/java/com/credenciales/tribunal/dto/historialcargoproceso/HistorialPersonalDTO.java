package com.credenciales.tribunal.dto.historialcargoproceso;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class HistorialPersonalDTO {
    private Long personalId;
    private String nombreCompleto;
    private String nombreCargo;
    private String nombreUnidad;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String nombreProcesoElectoral;
}