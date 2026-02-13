package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.historialcargo.HistorialCargoCreateRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoSearchRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoUpdateRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HistorialCargoService {
    
    // CRUD básico
    HistorialCargoResponseDTO createHistorial(HistorialCargoCreateRequestDTO requestDTO);
    
    Optional<HistorialCargoResponseDTO> getHistorialById(Long id);
    
    List<HistorialCargoResponseDTO> getAllHistoriales();
    
    Page<HistorialCargoResponseDTO> getAllHistorialesPaged(Pageable pageable);
    
    HistorialCargoResponseDTO updateHistorial(Long id, HistorialCargoUpdateRequestDTO requestDTO);
    
    void deleteHistorial(Long id);
    
    // Métodos de búsqueda por relaciones
    List<HistorialCargoResponseDTO> getHistorialesByPersonal(Long personalId);
    
    List<HistorialCargoResponseDTO> getHistorialesByCargo(Long cargoId);
    
    List<HistorialCargoResponseDTO> getHistorialesActivosByPersonal(Long personalId);
    
    List<HistorialCargoResponseDTO> getHistorialesActivosByCargo(Long cargoId);
    
    Optional<HistorialCargoResponseDTO> getHistorialActivoByPersonalAndCargo(Long personalId, Long cargoId);
    
    // Métodos de búsqueda por fechas
    List<HistorialCargoResponseDTO> getHistorialesByFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<HistorialCargoResponseDTO> getHistorialesFinalizadosEnRango(LocalDateTime start, LocalDateTime end);
    
    // Métodos de búsqueda avanzada
    List<HistorialCargoResponseDTO> searchHistoriales(HistorialCargoSearchRequestDTO searchRequest);
    
    // Métodos de negocio
    HistorialCargoResponseDTO finalizarHistorial(Long id, LocalDateTime fechaFin);
    
    HistorialCargoResponseDTO reactivarHistorial(Long id);
    
    // Métodos de validación
    boolean tieneHistorialActivoEnCargo(Long personalId, Long cargoId);
    
    Long countHistorialesActivosByPersonal(Long personalId);
    
    Long countHistorialesActivosByCargo(Long cargoId);
    
    // Métodos con relaciones completas
    Optional<HistorialCargoResponseDTO> getHistorialWithAllRelations(Long id);
    
    List<HistorialCargoResponseDTO> getHistorialesByPersonalWithDetails(Long personalId);
    
    List<HistorialCargoResponseDTO> getHistorialesByCargoWithDetails(Long cargoId);
}