package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.cargo.CargoRequestDTO;
import com.credenciales.tribunal.dto.cargo.CargoResponseDTO;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.exception.DuplicateResourceException;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.dto.cargo.CargoMapper;
import com.credenciales.tribunal.model.entity.Cargo;
import com.credenciales.tribunal.model.entity.Unidad;
import com.credenciales.tribunal.repository.CargoRepository;
import com.credenciales.tribunal.repository.UnidadRepository;
import com.credenciales.tribunal.service.CargoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CargoServiceImpl implements CargoService {
    
    private final CargoRepository cargoRepository;
    private final UnidadRepository unidadRepository;
    private final CargoMapper cargoMapper;
    
    @Override
    public CargoResponseDTO createCargo(CargoRequestDTO requestDTO) {
        log.info("Creando nuevo cargo: {} en unidad ID: {}", requestDTO.getNombre(), requestDTO.getUnidadId());
        
        // Validar que la unidad exista
        Unidad unidad = unidadRepository.findById(requestDTO.getUnidadId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada con ID: " + requestDTO.getUnidadId()));
        
        // Validar que la unidad esté activa
        if (!unidad.getEstado()) {
            throw new BusinessException("No se puede crear un cargo en una unidad inactiva");
        }
        
        // Validar que no exista un cargo con el mismo nombre en la misma unidad
        if (cargoRepository.existsByNombreAndUnidadId(requestDTO.getNombre(), requestDTO.getUnidadId())) {
            throw new DuplicateResourceException(
                String.format("Ya existe un cargo con nombre '%s' en la unidad '%s'", 
                    requestDTO.getNombre(), unidad.getNombre()));
        }
        
        Cargo cargo = cargoMapper.toEntity(requestDTO, unidad);
        Cargo savedCargo = cargoRepository.save(cargo);
        log.info("Cargo creado exitosamente con ID: {}", savedCargo.getId());
        
        return cargoMapper.toResponseDTO(savedCargo);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CargoResponseDTO> getCargoById(Long id) {
        log.debug("Buscando cargo por ID: {}", id);
        return cargoRepository.findById(id)
                .map(cargoMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoResponseDTO> getAllCargos() {
        log.debug("Obteniendo todos los cargos");
        List<Cargo> cargos = cargoRepository.findAllByOrderByNombreAsc();
        return cargoMapper.toResponseDTOList(cargos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CargoResponseDTO> getAllCargosPaged(Pageable pageable) {
        log.debug("Obteniendo cargos paginados");
        return cargoRepository.findAll(pageable)
                .map(cargoMapper::toResponseDTO);
    }
    
    @Override
    public CargoResponseDTO updateCargo(Long id, CargoRequestDTO requestDTO) {
        log.info("Actualizando cargo con ID: {}", id);
        
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado con ID: " + id));
        
        // Validar si se cambió la unidad
        Unidad nuevaUnidad = null;
        if (!cargo.getUnidad().getId().equals(requestDTO.getUnidadId())) {
            nuevaUnidad = unidadRepository.findById(requestDTO.getUnidadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada con ID: " + requestDTO.getUnidadId()));
            
            // Validar que la nueva unidad esté activa
            if (!nuevaUnidad.getEstado()) {
                throw new BusinessException("No se puede asignar un cargo a una unidad inactiva");
            }
        }
        
        // Validar duplicados si el nombre cambió o la unidad cambió
        if (!cargo.getNombre().equals(requestDTO.getNombre()) || nuevaUnidad != null) {
            Long unidadIdValidar = nuevaUnidad != null ? nuevaUnidad.getId() : cargo.getUnidad().getId();
            
            if (cargoRepository.existsByNombreAndUnidadId(requestDTO.getNombre(), unidadIdValidar)) {
                Unidad unidad = nuevaUnidad != null ? nuevaUnidad : cargo.getUnidad();
                throw new DuplicateResourceException(
                    String.format("Ya existe un cargo con nombre '%s' en la unidad '%s'", 
                        requestDTO.getNombre(), unidad.getNombre()));
            }
        }
        
        cargoMapper.updateEntity(requestDTO, cargo, nuevaUnidad);
        Cargo updatedCargo = cargoRepository.save(cargo);
        log.info("Cargo actualizado exitosamente con ID: {}", updatedCargo.getId());
        
        return cargoMapper.toResponseDTO(updatedCargo);
    }
    
    @Override
    public void deleteCargo(Long id) {
        log.info("Eliminando cargo con ID: {}", id);
        
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado con ID: " + id));
        
        // Validar si tiene historiales asociados
        if (cargo.getHistoriales() != null && !cargo.getHistoriales().isEmpty()) {
            throw new BusinessException("No se puede eliminar el cargo porque tiene historiales asociados");
        }
        
        cargoRepository.deleteById(id);
        log.info("Cargo eliminado exitosamente con ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoResponseDTO> getCargosByUnidad(Long unidadId) {
        log.debug("Buscando cargos por unidad ID: {}", unidadId);
        
        if (!unidadRepository.existsById(unidadId)) {
            throw new ResourceNotFoundException("Unidad no encontrada con ID: " + unidadId);
        }
        
        List<Cargo> cargos = cargoRepository.findByUnidadIdOrderByNombreAsc(unidadId);
        return cargoMapper.toResponseDTOList(cargos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CargoResponseDTO> getCargoByNombre(String nombre) {
        log.debug("Buscando cargo por nombre: {}", nombre);
        return cargoRepository.findByNombre(nombre)
                .map(cargoMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoResponseDTO> searchCargosByNombre(String nombre) {
        log.debug("Buscando cargos que contengan: {}", nombre);
        List<Cargo> cargos = cargoRepository.findByNombreContainingIgnoreCase(nombre);
        return cargoMapper.toResponseDTOList(cargos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CargoResponseDTO> getCargoWithHistorial(Long id) {
        log.debug("Buscando cargo con historial por ID: {}", id);
        return cargoRepository.findByIdWithHistorial(id)
                .map(cargoMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoResponseDTO> getCargosByUnidadWithHistorial(Long unidadId) {
        log.debug("Buscando cargos con historial por unidad ID: {}", unidadId);
        
        if (!unidadRepository.existsById(unidadId)) {
            throw new ResourceNotFoundException("Unidad no encontrada con ID: " + unidadId);
        }
        
        List<Cargo> cargos = cargoRepository.findByUnidadIdWithHistorial(unidadId);
        return cargoMapper.toResponseDTOList(cargos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countCargosByUnidad(Long unidadId) {
        log.debug("Contando cargos por unidad ID: {}", unidadId);
        
        if (!unidadRepository.existsById(unidadId)) {
            throw new ResourceNotFoundException("Unidad no encontrada con ID: " + unidadId);
        }
        
        return cargoRepository.countByUnidadId(unidadId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsCargoInUnidad(String nombre, Long unidadId) {
        log.debug("Verificando existencia de cargo '{}' en unidad ID: {}", nombre, unidadId);
        return cargoRepository.existsByNombreAndUnidadId(nombre, unidadId);
    }
}