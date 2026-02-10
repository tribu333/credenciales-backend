package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.login.LoginResponseDTO;
import com.credenciales.tribunal.dto.login.UsuarioLoginDTO;
import com.credenciales.tribunal.dto.usuario.UsuarioRegistroDTO;
import com.credenciales.tribunal.dto.usuario.UsuarioResponseDTO;
import com.credenciales.tribunal.model.RolUsuario;
import com.credenciales.tribunal.model.Usuario;
import com.credenciales.tribunal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    
    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRegistroDTO registroDTO) {
        log.info("Registrando nuevo usuario: {}", registroDTO.getUsername());
        
        // Verificar si el usuario ya existe
        if (usuarioRepository.existsByUsername(registroDTO.getUsername())) {
            throw new RuntimeException("El username ya está en uso");
        }
        
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new RuntimeException("El email ya está en uso");
        }
        
        // Determinar rol (por defecto CONSULTA)
        RolUsuario rol = RolUsuario.CONSULTA;
        if (registroDTO.getRol() != null) {
            try {
                rol = RolUsuario.valueOf(registroDTO.getRol().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Rol inválido: {}, usando CONSULTA por defecto", registroDTO.getRol());
            }
        }
        
        // Crear usuario
        Usuario usuario = Usuario.builder()
                .username(registroDTO.getUsername())
                .email(registroDTO.getEmail())
                .password(passwordEncoder.encode(registroDTO.getPassword()))
                .nombreCompleto(registroDTO.getNombreCompleto())
                .rol(rol)
                .activo(true)
                .build();
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Usuario registrado exitosamente: {}", usuarioGuardado.getUsername());
        
        return mapToResponseDTO(usuarioGuardado);
    }
    
    public LoginResponseDTO login(UsuarioLoginDTO loginDTO) {
        log.info("Intentando login para usuario: {}", loginDTO.getUsername());
        
        // Autenticar con Spring Security
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(),
                loginDTO.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Obtener usuario
        Usuario usuario = (Usuario) authentication.getPrincipal();
        
        // Actualizar último login
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);
        
        // Generar token JWT
        String jwtToken = jwtService.generateToken(usuario);
        
        log.info("Login exitoso para usuario: {}", usuario.getUsername());
        
        return LoginResponseDTO.builder()
                .token(jwtToken)
                .tipoToken("Bearer")
                .usuarioId(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol().name())
                .expiresIn(3600)
                .build();
    }
    
    public UsuarioResponseDTO getUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }
        
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return mapToResponseDTO(usuario);
    }
    
    private UsuarioResponseDTO mapToResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                .activo(usuario.isActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .ultimoLogin(usuario.getUltimoLogin())
                .build();
    }
}