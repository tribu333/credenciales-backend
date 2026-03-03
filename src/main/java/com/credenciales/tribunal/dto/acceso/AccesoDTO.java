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
public class AccesoDTO {
    private Long id;
    private LocalDateTime fechaHora;
    private TipoEventoAcceso tipoEvento;
    private Long qrId;
    private String qrCodigo; // Información adicional
    private Long asignacionQrId;
    private String asignacionQrDescripcion; // Información adicional
}

