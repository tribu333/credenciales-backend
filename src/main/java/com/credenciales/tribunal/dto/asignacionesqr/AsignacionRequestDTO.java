package com.credenciales.tribunal.dto.asignacionesqr;

import lombok.*;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor  // Genera constructor sin parámetros
@AllArgsConstructor
public class AsignacionRequestDTO {
    @NotNull(message = "El externoId no puede estar vacío")
    private Long externoId;
    @NotNull(message = "El qrId no puede ser nulo")
    private Long qrId;
    private LocalDateTime fechaLiberacion; // opcional
}