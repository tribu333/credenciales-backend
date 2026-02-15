package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoCreateRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoSearchRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoUpdateRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HistorialCargoProcesoService {
    
    // CRUD básico
    HistorialCargoProcesoResponseDTO createHistorial(HistorialCargoProcesoCreateRequestDTO requestDTO);
    
    Optional<HistorialCargoProcesoResponseDTO> getHistorialById(Long id);
    
    List<HistorialCargoProcesoResponseDTO> getAllHistoriales();
    
    Page<HistorialCargoProcesoResponseDTO> getAllHistorialesPaged(Pageable pageable);
    
    HistorialCargoProcesoResponseDTO updateHistorial(Long id, HistorialCargoProcesoUpdateRequestDTO requestDTO);
    
    void deleteHistorial(Long id);
    
    // Métodos de búsqueda por relaciones
    List<HistorialCargoProcesoResponseDTO> getHistorialesByCargoProceso(Long cargoProcesoId);
    
    List<HistorialCargoProcesoResponseDTO> getHistorialesByPersonal(Long personalId);
    
    List<HistorialCargoProcesoResponseDTO> getHistorialesByProceso(Long procesoId);
    
    List<HistorialCargoProcesoResponseDTO> getHistorialesByUnidad(Long unidadId);
    
    // Métodos de búsqueda de activos
    List<HistorialCargoProcesoResponseDTO> getHistorialesActivosByCargoProceso(Long cargoProcesoId);
    
    List<HistorialCargoProcesoResponseDTO> getHistorialesActivosByPersonal(Long personalId);
    
    List<HistorialCargoProcesoResponseDTO> getHistorialesActivosByProceso(Long procesoId);
    
    Optional<HistorialCargoProcesoResponseDTO> getHistorialActivoByPersonalAndCargoProceso(
            Long personalId, Long cargoProcesoId);
    
    // Métodos de búsqueda por fechas
    List<HistorialCargoProcesoResponseDTO> getHistorialesByRangoFechas(
            LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<HistorialCargoProcesoResponseDTO> getHistorialesFinalizadosEnRango(
            LocalDateTime start, LocalDateTime end);
    
    List<HistorialCargoProcesoResponseDTO> getHistorialesSinFechaFin();
    
    // Métodos de búsqueda avanzada
    List<HistorialCargoProcesoResponseDTO> searchHistoriales(
            HistorialCargoProcesoSearchRequestDTO searchRequest);
    
    // Métodos con relaciones
    Optional<HistorialCargoProcesoResponseDTO> getHistorialWithAllRelations(Long id);
    
    List<HistorialCargoProcesoResponseDTO> getHistorialesByCargoProcesoWithPersonal(Long cargoProcesoId);
    
    List<HistorialCargoProcesoResponseDTO> getHistorialesByPersonalWithCargoProceso(Long personalId);
    
    // Métodos de negocio
    HistorialCargoProcesoResponseDTO finalizarHistorial(Long id, LocalDateTime fechaFin);
    
    HistorialCargoProcesoResponseDTO reactivarHistorial(Long id);
    
    HistorialCargoProcesoResponseDTO asignarPersonalACargoProceso(
            Long personalId, Long cargoProcesoId, LocalDateTime fechaInicio);
    
    HistorialCargoProcesoResponseDTO reasignarPersonal(
            Long historialId, Long nuevoCargoProcesoId, LocalDateTime fechaReasignacion);
    
    // Métodos de validación
    boolean tieneHistorialActivoEnCargoProceso(Long personalId, Long cargoProcesoId);
    
    boolean puedeAsignarPersonalACargoProceso(Long personalId, Long cargoProcesoId);
    
    // Métodos de utilidad
    Long countHistorialesActivosByCargoProceso(Long cargoProcesoId);
    
    Long countHistorialesActivosByPersonal(Long personalId);
    
    Long countHistorialesActivosByProceso(Long procesoId);
    
    List<HistorialCargoProcesoResponseDTO> getUltimosHistorialesByPersonal(
            Long personalId, int limite);
}