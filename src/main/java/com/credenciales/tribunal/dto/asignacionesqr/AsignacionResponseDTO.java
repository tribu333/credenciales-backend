package com.credenciales.tribunal.dto.asignacionesqr;

import lombok.*;
import java.time.*;

@Data
@Builder
public class AsignacionResponseDTO {
    private Long id;
    private String externoNombre;
    private String qrCodigo;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaLiberacion;
    private Boolean activo;
}