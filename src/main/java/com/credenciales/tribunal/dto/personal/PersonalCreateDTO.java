package com.credenciales.tribunal.dto.personal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.credenciales.tribunal.model.enums.TipoPersonal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar un personal")
public class PersonalCreateDTO {
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombres del personal", example = "Juan Carlos", required = true)
    private String nombre;
    
    @Size(max = 100, message = "El apellido paterno no puede exceder los 100 caracteres")
    @Schema(description = "Apellido paterno", example = "Pérez")
    private String apellidoPaterno;
    
    @Size(max = 100, message = "El apellido materno no puede exceder los 100 caracteres")
    @Schema(description = "Apellido materno", example = "González")
    private String apellidoMaterno;
    
    @NotBlank(message = "El carnet de identidad es requerido")
    @Size(min = 5, max = 20, message = "El carnet de identidad debe tener entre 5 y 20 caracteres")
    @Pattern(regexp = "^[0-9]+[A-Za-z]?$", message = "El carnet de identidad debe contener solo números y opcionalmente una letra al final")
    @Schema(description = "Carnet de identidad", example = "1234567", required = true)
    private String carnetIdentidad;
    
    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "El correo electrónico debe tener un formato válido")
    @Size(max = 150, message = "El correo electrónico no puede exceder los 150 caracteres")
    @Schema(description = "Correo electrónico", example = "juan.perez@ejemplo.com", required = true)
    private String correo;
    
    @Pattern(regexp = "^[0-9]{7,10}$", message = "El celular debe contener entre 7 y 10 dígitos")
    @Schema(description = "Número de celular", example = "76543210")
    private String celular;
    
    @NotNull(message = "El campo acceso a cómputo es requerido")
    @Schema(description = "Tiene acceso a cómputo", example = "false", defaultValue = "false")
    private Boolean accesoComputo;
    
    @Size(max = 10, message = "El número de circunscripción no puede exceder los 10 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "El número de circunscripción solo puede contener letras, números y guiones")
    @Schema(description = "Número de circunscripción", example = "C-2")
    private String nroCircunscripcion;
    
    @NotNull(message = "El tipo de personal es requerido")
    @Schema(description = "Tipo de personal", example = "JURADO")
    private TipoPersonal tipo;
    
    @Schema(description = "ID de la imagen asociada", example = "1")
    private Long imagenId;
}
