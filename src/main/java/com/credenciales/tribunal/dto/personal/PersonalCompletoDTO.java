package com.credenciales.tribunal.dto.personal;

import com.credenciales.tribunal.model.enums.TipoPersonal;

import io.swagger.v3.oas.annotations.media.Schema;

import com.credenciales.tribunal.dto.image.ImagenResponseDTO;
import com.credenciales.tribunal.dto.qr.QrResponseDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoDTO;
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
@Schema(description = "DTO completo de personal con todas sus relaciones")
public class PersonalCompletoDTO {
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
    private String codigoVerificacion;
    private LocalDateTime createdAt;
    
    // Relaciones
    private Long imagenId;
    private Long qrId;
}
