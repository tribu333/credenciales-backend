package com.credenciales.tribunal.dto.cargoproceso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoProcesoResponseDTO {
    private Long id;
    private Long procesoId;
    private String nombre;
    private String descripcion;
    private Long unidadId;
}
