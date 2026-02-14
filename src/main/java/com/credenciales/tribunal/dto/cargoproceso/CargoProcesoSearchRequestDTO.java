package com.credenciales.tribunal.dto.cargoproceso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoProcesoSearchRequestDTO {
    
    private Long procesoId;
    
    private Long unidadId;
    
    private String nombre;
    
    private Boolean activo;
    
    private Boolean conHistoriales; // true = solo con historiales, false = solo sin historiales
}