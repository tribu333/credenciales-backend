package com.credenciales.tribunal.dto.historialcargoproceso;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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
    //@JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime fechaInicio;
    
    @NotNull(message = "La fecha de fin es requerida")
    //@JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime fechaFin;
}
