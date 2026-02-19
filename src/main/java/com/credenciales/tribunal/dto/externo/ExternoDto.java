package com.credenciales.tribunal.dto.externo;

import com.credenciales.tribunal.model.enums.TipoExterno;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternoDto {

    private Long id;
    private String nombreCompleto;
    private String carnetIdentidad;
    private String identificador;
    private String orgPolitica;
    private TipoExterno tipoExterno;
    private LocalDateTime createdAt;
    private Long idImagen;
    private List<AsignacionQrBasicaDTO> asignaciones; // DTO básico de asignaciones
}

