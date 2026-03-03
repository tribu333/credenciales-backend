package com.credenciales.tribunal.dto.acceso;

import com.credenciales.tribunal.model.enums.TipoEventoAcceso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionQrInfoDTO {
    private Long id;
    // private String descripcion;
    private Boolean activo;
}

