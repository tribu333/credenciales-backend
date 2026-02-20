package com.credenciales.tribunal.dto.asignacionesqr;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class AsignacionRequestDTO {
    private Long externoId;
    private Long qrId;
    private LocalDateTime fechaLiberacion; // opcional
}