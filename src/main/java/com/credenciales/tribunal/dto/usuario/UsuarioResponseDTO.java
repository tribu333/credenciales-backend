package com.credenciales.tribunal.dto.usuario;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.credenciales.tribunal.model.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    
    private Long id;
    private String username;
    private String email;
    private String nombreCompleto;
    private RolUsuario rol;
    private boolean activo;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ultimoLogin;
}