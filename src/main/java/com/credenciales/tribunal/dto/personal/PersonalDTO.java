package com.credenciales.tribunal.dto.personal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.credenciales.tribunal.model.enums.TipoPersonal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO completo del personal con su estado actual")
public class PersonalDTO {
    
    @Schema(description = "ID único del personal", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @Schema(description = "Nombres del personal", example = "Juan Carlos", required = true, maxLength = 100)
    private String nombre;
    
    @Schema(description = "Apellido paterno", example = "Pérez", maxLength = 100)
    private String apellidoPaterno;
    
    @Schema(description = "Apellido materno", example = "González", maxLength = 100)
    private String apellidoMaterno;
    
    @Schema(description = "Carnet de identidad", example = "1234567", required = true)
    private String carnetIdentidad;
    
    @Schema(description = "Correo electrónico", example = "juan.perez@ejemplo.com", required = true)
    private String correo;
    
    @Schema(description = "Número de celular", example = "76543210", pattern = "^[0-9]{8}$")
    private String celular;
    
    @Schema(description = "Tiene acceso a cómputo", example = "false", defaultValue = "false")
    private Boolean accesoComputo;
    
    @Schema(description = "Número de circunscripción", example = "C-2", maxLength = 10)
    private String nroCircunscripcion;
    
    @NotBlank(message = "Tipo es requerido")
    private TipoPersonal tipo;
    
    @Schema(description = "ID de la imagen asociada", example = "1")
    private Long imagenId;
    
    @Schema(description = "ID del código QR asociado", example = "1")
    private Long qrId;
    
    @Schema(description = "Estado actual del personal", 
            example = "PERSONAL ACTIVO", 
            allowableValues = {"PERSONAL REGISTRADO", "CREDENCIAL IMPRESO", "CREDENCIAL ENTREGADO", 
                            "PERSONAL ACTIVO", "PERSONAL CON ACCESO A COMPUTO", "CREDENCIAL DEVUELTO", 
                            "INACTIVO PROCESO ELECTORAL TERMINADO", "INACTIVO POR RENUNCIA"})
    private String estadoActual;
    
    @Schema(description = "Fecha de creación del registro", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}