package com.credenciales.tribunal.dto.externo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionQrBasicaDTO {
    private Long id;
    private String codigoQr;
    private LocalDateTime fechaAsignacion;
    private Boolean activo;
}