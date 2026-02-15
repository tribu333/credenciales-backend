package com.credenciales.tribunal.dto.cargoproceso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoProcesoCreateRequestDTO {
    
    @NotNull(message = "El proceso electoral es obligatorio")
    private Long procesoId;
    
    @NotBlank(message = "El nombre del cargo es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
    private String nombre;
    
    @NotNull(message = "La unidad es obligatoria")
    private Long unidadId;
    
    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String descripcion;
    
    @Builder.Default
    private Boolean activo = true;
}