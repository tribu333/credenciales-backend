package com.credenciales.tribunal.dto.estadoActual;

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
public class CambioEstadoResquestDTO {

    @NotBlank(message = "ID del personal es requerido")
    @Size(min = 1, message = "ID del personal debe ser un número válido")
    private Long personalId;

    @Builder.Default
    private Boolean habilitarAccesoComputo = true;
}