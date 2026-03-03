package com.credenciales.tribunal.dto.asignacionesqr;

import lombok.*;
import java.time.LocalDateTime;

import com.credenciales.tribunal.dto.externo.ExternoDTO;

@Data
@Builder
@NoArgsConstructor  // Genera constructor sin parámetros
@AllArgsConstructor
public class AsignacionResponseDetalDTO {
    private Long id;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaLiberacion;
    private Boolean activo;
    private ExternoDTO externo;
}
