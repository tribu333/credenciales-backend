package com.credenciales.tribunal.dto.unidad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnidadRequestDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 250, message = "El nombre no puede exceder los 250 caracteres")
    private String nombre;
    
    @NotBlank(message = "La abreviatura es obligatoria")
    @Size(max = 50, message = "La abreviatura no puede exceder los 50 caracteres")
    private String abreviatura;
    
    @Builder.Default
    private Boolean estado = true;
}