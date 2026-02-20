package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.cargo.CargoRequestDTO;
import com.credenciales.tribunal.dto.cargo.CargoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CargoService {
    
    // CRUD básico
    CargoResponseDTO createCargo(CargoRequestDTO requestDTO);
    
    Optional<CargoResponseDTO> getCargoById(Long id);
    
    List<CargoResponseDTO> getAllCargos();
    
    List<CargoResponseDto> getAllCargosProceso();

    List<CargoResponseDto> getAllCargosProcesoByUnidad(long id);

    Page<CargoResponseDTO> getAllCargosPaged(Pageable pageable);
    
    CargoResponseDTO updateCargo(Long id, CargoRequestDTO requestDTO);
    
    void deleteCargo(Long id);
    
    // Métodos de búsqueda
    List<CargoResponseDTO> getCargosByUnidad(Long unidadId);
    
    Optional<CargoResponseDTO> getCargoByNombre(String nombre);
    
    List<CargoResponseDTO> searchCargosByNombre(String nombre);
    
    // Métodos con relaciones
    Optional<CargoResponseDTO> getCargoWithHistorial(Long id);
    
    List<CargoResponseDTO> getCargosByUnidadWithHistorial(Long unidadId);
    
    // Métodos de utilidad
    Long countCargosByUnidad(Long unidadId);
    
    boolean existsCargoInUnidad(String nombre, Long unidadId);
}