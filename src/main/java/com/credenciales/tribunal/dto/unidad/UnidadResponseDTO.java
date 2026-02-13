package com.credenciales.tribunal.dto.unidad;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnidadResponseDTO {
    private Long id;
    private String nombre;
    private String abreviatura;
    private Boolean estado;
    private LocalDateTime createdAt;
    private Integer totalCargos;
    private Integer totalCargosProceso;
}