package com.credenciales.tribunal.dto.personal;

import com.credenciales.tribunal.model.enums.TipoPersonal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO completo de personal para acceso")
public class PersonalAccesoDTO {
    private Long id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String carnetIdentidad;
    private Boolean accesoComputo;
    private TipoPersonal tipo;
    private String estadoActual;
    private LocalDateTime createdAt;
    private String cargo;
    private String unidad;
    private String imagen;//ruta imagen
    private String evento;
}