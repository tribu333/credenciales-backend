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
public class CargoProcesoResumeDTO {
    private Long id;
    private Long procesoId;
    private String nombre;
    private Long unidadId;
    private Boolean activo;
    private Long totalHistorialesActivos;

}
