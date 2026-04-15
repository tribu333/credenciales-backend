package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.login.LoginResponseDTO;
import com.credenciales.tribunal.dto.login.UsuarioLoginDTO;
import com.credenciales.tribunal.dto.usuario.UsuarioRegistroDTO;
import com.credenciales.tribunal.dto.usuario.UsuarioResponseDTO;
import com.credenciales.tribunal.dto.usuario.UsuarioUpdateDTO;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.model.RolUsuario;
import com.credenciales.tribunal.model.Usuario;
import com.credenciales.tribunal.model.entity.Unidad;
import com.credenciales.tribunal.repository.UnidadRepository;
import com.credenciales.tribunal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final UnidadRepository unidadRepository;
    
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
        Unidad unidad=null;
        if(registroDTO.getUnidadId()!= null){
            if(!unidadRepository.existsById(registroDTO.getUnidadId())){
                throw new ResourceNotFoundException("La unidad con el id: {}" +registroDTO.getUnidadId());
            }else{
                unidad=unidadRepository.findById(registroDTO.getUnidadId()).get();
            }
        }
        
        //Unidad unidad= 
        
        // Crear usuario
        Usuario usuario = Usuario.builder()
                .username(registroDTO.getUsername())
                .email(registroDTO.getEmail())
                .password(passwordEncoder.encode(registroDTO.getPassword()))
                .nombreCompleto(registroDTO.getNombreCompleto())
                .rol(rol)
                .activo(true)
                .unidad(unidad)
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
                .idUnidad(usuario.getUnidad() != null ? usuario.getUnidad().getId() : null)
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
                .descripcion(usuario.getDescripcion())
                .rol(usuario.getRol())
                .activo(usuario.isActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .ultimoLogin(usuario.getUltimoLogin())
                .build();
    }
    // En AuthService.java
@Transactional
public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioUpdateDTO updateDTO, UserDetails currentUser) {
    log.info("Actualizando usuario con ID: {}", id);
    
    /* // Verificar permisos
    Usuario usuarioActual = (Usuario) currentUser;
    boolean isAdmin = usuarioActual.getRol() == RolUsuario.ADMINISTRADOR;
    boolean isSameUser = usuarioActual.getId().equals(id);
    
    if (!isAdmin && !isSameUser) {
        throw new SecurityException("No tienes permiso para actualizar este usuario");
    }
     */
    // Buscar usuario existente
    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    
    // Actualizar campos (solo los que no son null)
    if (updateDTO.getUsername() != null && !updateDTO.getUsername().equals(usuario.getUsername())) {
        if (usuarioRepository.existsByUsername(updateDTO.getUsername())) {
            throw new RuntimeException("El username ya está en uso");
        }
        usuario.setUsername(updateDTO.getUsername());
    }
    
    if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(usuario.getEmail())) {
        if (usuarioRepository.existsByEmail(updateDTO.getEmail())) {
            throw new RuntimeException("El email ya está en uso");
        }
        usuario.setEmail(updateDTO.getEmail());
    }
    
    if (updateDTO.getPassword() != null && !updateDTO.getPassword().trim().isEmpty()) {
        usuario.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
    }
    
    if (updateDTO.getNombreCompleto() != null) {
        usuario.setNombreCompleto(updateDTO.getNombreCompleto());
    }
    
    if (updateDTO.getDescripcion() != null) {
        usuario.setDescripcion(updateDTO.getDescripcion());
    }
    /* 
    // Actualizar rol (solo administradores)
    if (updateDTO.getRol() != null && isAdmin) {
        try {
            RolUsuario nuevoRol = RolUsuario.valueOf(updateDTO.getRol().toUpperCase());
            usuario.setRol(nuevoRol);
        } catch (IllegalArgumentException e) {
            log.warn("Rol inválido: {}", updateDTO.getRol());
        }
    }
    
    // Actualizar estado activo (solo administradores)
    if (updateDTO.getActivo() != null && isAdmin) {
        usuario.setActivo(updateDTO.getActivo());
    }
    
    // Actualizar unidad (solo administradores o si es el mismo usuario)
    if (updateDTO.getUnidadId() != null && (isAdmin || isSameUser)) {
        if (updateDTO.getUnidadId() == 0) {
            usuario.setUnidad(null);
        } else {
            Unidad unidad = unidadRepository.findById(updateDTO.getUnidadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada con ID: " + updateDTO.getUnidadId()));
            usuario.setUnidad(unidad);
        }
    } */
    
    // Actualizar timestamp automáticamente (lo hace @UpdateTimestamp)
    Usuario usuarioActualizado = usuarioRepository.save(usuario);
    log.info("Usuario actualizado exitosamente: {}", usuarioActualizado.getUsername());
    
    return mapToResponseDTO(usuarioActualizado);
}
}