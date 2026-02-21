package com.credenciales.tribunal.dto.personal;

import com.credenciales.tribunal.model.enums.TipoPersonal;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "")
public class PersonalNotarioDTO {
    private Long id;
    private String nombreCompleto;
    private String carnetIdentidad;
    private String correo;
    private String celular;
    private String nroCircunscripcion;
    private TipoPersonal tipo;
}
