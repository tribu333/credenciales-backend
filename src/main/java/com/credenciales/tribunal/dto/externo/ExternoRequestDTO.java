package com.credenciales.tribunal.dto.externo;

import com.credenciales.tribunal.model.enums.TipoExterno;
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
public class ExternoRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre completo no puede exceder los 150 caracteres")
    private String nombreCompleto;
    
    @NotBlank(message = "El carnet es obligatorio")
    @Size(max = 50, message = "El carnet de identidad no puede exceder los 50 caracteres")
    private String carnetIdentidad;

    @Size(max = 100, message = "El identificadorPrensa no puede exceder los 100 caracteres")
    private String identificadorPrensa;

    @Size(max = 150, message = "La organización política no puede exceder los 150 caracteres")
    private String orgPolitica;
    
    @Size(max = 20, message = "El nro celular no puede exceder los 20 caracteres")
    private String nroCelular;

    @NotNull(message = "El tipo de externo es obligatorio")
    private TipoExterno tipoExterno;

    private Long imagenId; // ID de la imagen asociada (opcional)
}