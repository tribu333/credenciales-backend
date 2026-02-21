package com.credenciales.tribunal.dto.estadoActual;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class CambioEstadoMasivoRequestDTO {
    @NotEmpty(message = "La lista de IDs no puede estar vacía")
    private List<Long> personalIds;
    
    private String observacion;
}