package com.credenciales.tribunal.dto.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    
    private String token;
    private String tipoToken;
    private Long usuarioId;
    private String username;
    private String nombreCompleto;
    private String rol;
    
    @Builder.Default
    private long expiresIn = 3600; // 1 hora en segundos
}
