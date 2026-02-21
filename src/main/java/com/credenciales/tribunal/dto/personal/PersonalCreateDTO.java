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
    @Schema(description = "Carnet de identidad", example = "1234567", required = true)
    private String carnetIdentidad;
    
    @Email(message = "El correo electrónico debe tener un formato válido")
    @Size(max = 150, message = "El correo electrónico no puede exceder los 150 caracteres")
    @Schema(description = "Correo electrónico", example = "juan.perez@ejemplo.com")
    private String correo;
    
    @Pattern(regexp = "^[0-9]{7,10}$", message = "El celular debe contener entre 7 y 10 dígitos")
    @Schema(description = "Número de celular", example = "76543210")
    private String celular;
    
    @Schema(description = "Tiene acceso a cómputo", example = "false", defaultValue = "false")
    @Builder.Default
    private Boolean accesoComputo = false;

    @Size(max = 10, message = "El número de circunscripción no puede exceder los 10 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]*$", message = "El número de circunscripción solo puede contener letras, números y guiones")
    @Schema(description = "Número de circunscripción", example = "C-2", nullable = true)
    private String nroCircunscripcion;

    @NotNull(message = "El cargo es requerido")
    @Schema(description = "ID del cargo", example = "1")
    private Long cargoID;

    @NotNull(message = "El tipo de personal es requerido")
    @Schema(description = "Tipo de personal", example = "EVENTUAL")
    @Builder.Default
    private TipoPersonal tipo = TipoPersonal.EVENTUAL;
    
    @NotNull(message = "La imagen es obligatoria")  // ← Cambiado de @NotBlank a @NotNull
    @Schema(description = "ID de la imagen", example = "1")
    private Long imagenId;

    @NotBlank(message = "El código de verificación es requerido")
    @Size(min = 6, max = 6, message = "El código de verificación debe tener 6 dígitos")
    @Pattern(regexp = "^[0-9]{6}$", message = "El código de verificación debe tener 6 dígitos")
    @Schema(description = "Código de verificación", example = "123456")
    private String codigoVerificacion;
}