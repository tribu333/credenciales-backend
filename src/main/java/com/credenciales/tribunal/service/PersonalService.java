package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.email.VerificacionCodigoRequestDTO;
import com.credenciales.tribunal.dto.email.VerificacionEmailRequestDTO;
import com.credenciales.tribunal.dto.email.VerificacionResponseDTO;
import com.credenciales.tribunal.dto.personal.PersonalActualizacionDTO;
import com.credenciales.tribunal.dto.personal.PersonalCompletoDTO;
import com.credenciales.tribunal.dto.personal.PersonalCreateDTO;
import com.credenciales.tribunal.model.enums.EstadoPersonal;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface PersonalService {
    
    // Verificación por email
    VerificacionResponseDTO solicitarCodigoVerificacion(VerificacionEmailRequestDTO request);
    boolean verificarCodigo(VerificacionCodigoRequestDTO request);
    
    // Registro completo con ID de imagen (sin MultipartFile)
    PersonalCompletoDTO registrarPersonalCompleto(PersonalCreateDTO registroDTO);
    
    // Consultas
    PersonalCompletoDTO obtenerPersonalPorId(Long id);
    PersonalCompletoDTO obtenerPersonalPorCarnet(String carnetIdentidad);
    PersonalCompletoDTO obtenerPersonalPorCorreo(String correo);
    List<PersonalCompletoDTO> listarTodos();
    List<PersonalCompletoDTO> listarPorTipo(String tipo);
    List<PersonalCompletoDTO> listarPorEstado(EstadoPersonal estado);
    
    // Validaciones
    boolean existeCorreoActivo(String correo);
    boolean puedeRegistrarseNuevamente(String carnetIdentidad);
    String obtenerMensajeEstadoActual(String carnetIdentidad);
    
    // Actualización (sin MultipartFile)
    PersonalCompletoDTO actualizarPersonal(Long id, PersonalCreateDTO actualizacionDTO);
    
    // Búsqueda
    List<PersonalCompletoDTO> buscarPorNombre(String nombre);

    PersonalCompletoDTO actualizarPersonalExistenteAdmin(Long id, PersonalActualizacionDTO actualizacionDTO);
}