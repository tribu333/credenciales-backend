package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralCreateRequestDTO;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralSearchRequestDTO;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralUpdateRequestDTO;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProcesoElectoralService {
    
    // CRUD básico
    ProcesoElectoralResponseDTO createProceso(ProcesoElectoralCreateRequestDTO requestDTO);
    
    Optional<ProcesoElectoralResponseDTO> getProcesoById(Long id);
    
    List<ProcesoElectoralResponseDTO> getAllProcesos();
    
    Page<ProcesoElectoralResponseDTO> getAllProcesosPaged(Pageable pageable);
    
    ProcesoElectoralResponseDTO updateProceso(Long id, ProcesoElectoralUpdateRequestDTO requestDTO);
    
    void deleteProceso(Long id);
    
    // Métodos de búsqueda
    Optional<ProcesoElectoralResponseDTO> getProcesoByNombre(String nombre);
    
    List<ProcesoElectoralResponseDTO> searchProcesosByNombre(String nombre);
    
    List<ProcesoElectoralResponseDTO> getProcesosByEstado(Boolean estado);
    
    List<ProcesoElectoralResponseDTO> getProcesosActivos();
    
    // Métodos de búsqueda por fechas
    List<ProcesoElectoralResponseDTO> getProcesosByRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);
    
    List<ProcesoElectoralResponseDTO> getProcesosVigentes(LocalDate fecha);
    
    List<ProcesoElectoralResponseDTO> getProcesosVigentesActuales();
    
    List<ProcesoElectoralResponseDTO> getProcesosProximos();
    
    List<ProcesoElectoralResponseDTO> getProcesosFinalizados();
    
    // Métodos de búsqueda avanzada
    List<ProcesoElectoralResponseDTO> searchProcesos(ProcesoElectoralSearchRequestDTO searchRequest);
    
    // Métodos con relaciones
    Optional<ProcesoElectoralResponseDTO> getProcesoWithImagen(Long id);
    
    Optional<ProcesoElectoralResponseDTO> getProcesoWithCargos(Long id);
    
    Optional<ProcesoElectoralResponseDTO> getProcesoWithAllRelations(Long id);
    
    // Métodos de negocio
    ProcesoElectoralResponseDTO activarProceso(Long id);
    
    ProcesoElectoralResponseDTO desactivarProceso(Long id);
    
    ProcesoElectoralResponseDTO extenderFechaFin(Long id, LocalDate nuevaFechaFin);
    
    boolean isProcesoVigente(Long id);
    
    // Métodos de utilidad
    Long countProcesosByEstado(Boolean estado);
    
    Optional<ProcesoElectoralResponseDTO> getUltimoProceso();
}