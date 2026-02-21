package com.credenciales.tribunal.dto.personal;

import com.credenciales.tribunal.model.enums.TipoPersonal;
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
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para registro de personal con verificación")
public class PersonalRegistroCompletoDTO {
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100)
    private String nombre;
    
    @Size(max = 100)
    private String apellidoPaterno;
    
    @Size(max = 100)
    private String apellidoMaterno;
    
    @NotBlank(message = "El carnet de identidad es requerido")
    @Size(min = 5, max = 20)
    private String carnetIdentidad;
    
    private String correo;
    
    @Pattern(regexp = "^[0-9]{7,10}$")
    private String celular;
    
    @NotNull(message = "El campo acceso a cómputo es requerido")
    private Boolean accesoComputo;
    
    @Size(max = 10)
    private String nroCircunscripcion;
    
    @NotNull(message = "El tipo de personal es requerido")
    private TipoPersonal tipo;
    
    @NotNull(message = "El código de verificación es requerido")
    @Pattern(regexp = "^[0-9]{6}$")
    private String codigoVerificacion;
    
    @Schema(description = "ID del cargo para personal de PLANTA")
    private Long cargoId;
    
    @Schema(description = "ID del cargo proceso para personal EVENTUAL")
    private Long cargoProcesoId;
}
