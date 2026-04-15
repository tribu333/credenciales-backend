package com.credenciales.tribunal.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;
    
    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no puede exceder 100 caracteres")
    private String email;
    
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;
    
    @Size(min = 2, max = 100, message = "El nombre completo debe tener entre 2 y 100 caracteres")
    private String nombreCompleto;
    
    @Size(max = 180, message = "La descripción no puede exceder 180 caracteres")
    private String descripcion;
    
    private String rol;
    
    private Boolean activo;
    
    private Long unidadId;
}
