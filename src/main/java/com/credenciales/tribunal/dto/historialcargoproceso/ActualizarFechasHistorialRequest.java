package com.credenciales.tribunal.dto.historialcargoproceso;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarFechasHistorialRequest {
    @NotNull(message = "El ID del proceso electoral es requerido")
    private Long procesoElectoralId;
    
    @NotNull(message = "El ID del cargo proceso es requerido")
    private Long cargoProcesoId;
    
    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDateTime fechaInicio;
    
    @NotNull(message = "La fecha de fin es requerida")
    private LocalDateTime fechaFin;
}
