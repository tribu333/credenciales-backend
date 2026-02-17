package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoCreateRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoSearchRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoUpdateRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CargoProcesoService {
    
    // CRUD básico
    CargoProcesoResponseDTO createCargoProceso(CargoProcesoCreateRequestDTO requestDTO);
    
    Optional<CargoProcesoResponseDTO> getCargoProcesoById(Long id);
    
    List<CargoProcesoResponseDTO> getAllCargosProceso();
    
    Page<CargoProcesoResponseDTO> getAllCargosProcesoPaged(Pageable pageable);
    
    CargoProcesoResponseDTO updateCargoProceso(Long id, CargoProcesoUpdateRequestDTO requestDTO);
    
    void deleteCargoProceso(Long id);
    
    // Métodos de búsqueda por relaciones
    List<CargoProcesoResponseDTO> getCargosProcesoByProceso(Long procesoId);
    
    List<CargoProcesoResponseDTO> getCargosProcesoByUnidad(Long unidadId);
    
    List<CargoProcesoResponseDTO> getCargosProcesoActivosByProceso(Long procesoId);
    
    List<CargoProcesoResponseDTO> getCargosProcesoByProcesoAndUnidad(Long procesoId, Long unidadId);
    
    // Métodos de búsqueda por nombre
    Optional<CargoProcesoResponseDTO> getCargoProcesoByProcesoAndNombre(Long procesoId, String nombre);
    
    List<CargoProcesoResponseDTO> searchCargosProcesoByNombre(String nombre);
    
    // Métodos de búsqueda avanzada
    List<CargoProcesoResponseDTO> searchCargosProceso(CargoProcesoSearchRequestDTO searchRequest);
    
    // Métodos con relaciones
    Optional<CargoProcesoResponseDTO> getCargoProcesoWithAllRelations(Long id);
    
    List<CargoProcesoResponseDTO> getCargosProcesoByProcesoWithRelations(Long procesoId);
    
    List<CargoProcesoResponseDTO> getCargosProcesoWithHistorialCount(Long procesoId);
    
    // Métodos de negocio
    CargoProcesoResponseDTO activarCargoProceso(Long id);
    
    CargoProcesoResponseDTO desactivarCargoProceso(Long id);
    
    boolean tieneHistorialesActivos(Long id);
    
    // Métodos de utilidad
    Long countCargosProcesoByProceso(Long procesoId);
    
    Long countCargosProcesoByUnidad(Long unidadId);
    
    boolean existsCargoProcesoInProceso(Long procesoId, String nombre);

    List<CargoProcesoResponseDTO> createCargosSimple(List<CargoProcesoCreateRequestDTO> cargos);
}