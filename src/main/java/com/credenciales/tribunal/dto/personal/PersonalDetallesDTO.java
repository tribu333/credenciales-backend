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
@Schema(description = "DTO completo de personal con todas sus relaciones")
public class PersonalDetallesDTO {
    private Long id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String carnetIdentidad;
    private String correo;
    private String celular;
    private Boolean accesoComputo;
    private String nroCircunscripcion;
    private TipoPersonal tipo;
    private String estadoActual;
    private LocalDateTime createdAt;
    private String cargo;
    private String unidad;
    private Long imagenId;
    private Long qrId;
    private String imagen;//ruta imagen
    private String qr;//ruta qr
}
