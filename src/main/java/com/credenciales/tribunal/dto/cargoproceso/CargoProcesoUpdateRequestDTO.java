package com.credenciales.tribunal.dto.cargoproceso;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoProcesoUpdateRequestDTO {
    
    @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
    private String nombre;
    
    private Long unidadId;
    
    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String descripcion;
    
    private Boolean activo;
}