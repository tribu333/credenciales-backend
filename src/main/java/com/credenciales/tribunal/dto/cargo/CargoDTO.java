package com.credenciales.tribunal.dto.cargo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoDTO {
    private Long id;
    private String nombre;
    private Long unidadId;
    private String unidadNombre;
    private String unidadAbreviatura;
    private LocalDateTime createdAt;
}
