package com.credenciales.tribunal.dto.unidad;


import java.util.List;

import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoResumeDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnidadProcesosList {
    private Long id;
    private String nombre;
    private String abreviatura;
    private Boolean estado;
    List<CargoProcesoResumeDTO> cargos;
}
