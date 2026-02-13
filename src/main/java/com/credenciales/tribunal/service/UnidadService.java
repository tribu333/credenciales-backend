package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.unidad.UnidadDTO;
import com.credenciales.tribunal.dto.unidad.UnidadRequestDTO;
import com.credenciales.tribunal.dto.unidad.UnidadResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface UnidadService {
    
    // CRUD básico
    UnidadResponseDTO createUnidad(UnidadRequestDTO unidadRequestDTO);
    
    Optional<UnidadResponseDTO> getUnidadById(Long id);
    
    List<UnidadResponseDTO> getAllUnidades();
    
    Page<UnidadResponseDTO> getAllUnidadesPaged(Pageable pageable);
    
    UnidadResponseDTO updateUnidad(Long id, UnidadRequestDTO unidadRequestDTO);
    
    void deleteUnidad(Long id);
    
    // Métodos adicionales
    List<UnidadResponseDTO> getUnidadesByEstado(Boolean estado);
    
    Optional<UnidadResponseDTO> getUnidadByNombre(String nombre);
    
    Optional<UnidadResponseDTO> getUnidadByAbreviatura(String abreviatura);
    
    List<UnidadResponseDTO> searchUnidadesByNombre(String nombre);
    
    UnidadResponseDTO cambiarEstadoUnidad(Long id, Boolean estado);
    
    // Métodos con relaciones
    Optional<UnidadResponseDTO> getUnidadWithCargos(Long id);
    
    Optional<UnidadResponseDTO> getUnidadWithCargosProceso(Long id);
}