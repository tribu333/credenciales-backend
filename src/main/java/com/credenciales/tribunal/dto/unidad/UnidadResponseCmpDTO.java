package com.credenciales.tribunal.dto.unidad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoResumeDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnidadResponseCmpDTO {
    private Long id;
    private String nombre;
    private String abreviatura;
    private Boolean estado;
    private LocalDateTime createdAt;
    private Integer totalCargos;
    private Integer totalCargosProceso;
    private List<CargoProcesoResumeDTO> procesos;
}