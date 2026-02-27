package com.credenciales.tribunal.dto.contrato;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoCreateRequestDTO {
    
    @NotNull(message = "El ID del personal es obligatorio")
    @Positive(message = "El ID del personal debe ser positivo")
    private Long personalId;
    
    private Boolean activo;
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado")
    private LocalDateTime fechaInicio;
    
    @FutureOrPresent(message = "La fecha de fin no puede ser en el pasado")
    private LocalDateTime fechaFin;
    
    @NotNull(message = "El ID del cargo es obligatorio")
    @Positive(message = "El ID del cargo debe ser positivo")
    private Long cargoId;
    
    @Positive(message = "El ID del proceso debe ser positivo")
    private Long procesoId;
}
