package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoCreateRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoSearchRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoUpdateRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoResponseDTO;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.exception.DuplicateResourceException;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoMapper;
import com.credenciales.tribunal.model.entity.CargoProceso;
import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import com.credenciales.tribunal.model.entity.Unidad;
import com.credenciales.tribunal.repository.CargoProcesoRepository;
import com.credenciales.tribunal.repository.ProcesoElectoralRepository;
import com.credenciales.tribunal.repository.UnidadRepository;
import com.credenciales.tribunal.service.CargoProcesoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CargoProcesoServiceImpl implements CargoProcesoService {
    
    private final CargoProcesoRepository cargoProcesoRepository;
    private final ProcesoElectoralRepository procesoRepository;
    private final UnidadRepository unidadRepository;
    private final CargoProcesoMapper cargoProcesoMapper;
    
    @Override
    public CargoProcesoResponseDTO createCargoProceso(CargoProcesoCreateRequestDTO requestDTO) {
        log.info("Creando nuevo cargo proceso: {} en proceso ID: {}", 
                requestDTO.getNombre(), requestDTO.getProcesoId());
        
        // Validar que el proceso exista y esté activo
        ProcesoElectoral proceso = procesoRepository.findById(requestDTO.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso electoral no encontrado con ID: " + requestDTO.getProcesoId()));
        
        if (!proceso.getEstado()) {
            throw new BusinessException("No se puede crear un cargo en un proceso inactivo");
        }
        
        // Validar que la unidad exista y esté activa
        Unidad unidad = unidadRepository.findById(requestDTO.getUnidadId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unidad no encontrada con ID: " + requestDTO.getUnidadId()));
        
        if (!unidad.getEstado()) {
            throw new BusinessException("No se puede crear un cargo en una unidad inactiva");
        }
        
        // Validar que no exista un cargo con el mismo nombre en el mismo proceso
        if (cargoProcesoRepository.existsByProcesoIdAndNombre(
                requestDTO.getProcesoId(), requestDTO.getNombre())) {
            throw new DuplicateResourceException(
                    String.format("Ya existe un cargo con nombre '%s' en el proceso '%s'", 
                            requestDTO.getNombre(), proceso.getNombre()));
        }
        
        CargoProceso cargoProceso = cargoProcesoMapper.toEntity(requestDTO, proceso, unidad);
        CargoProceso savedCargoProceso = cargoProcesoRepository.save(cargoProceso);
        log.info("Cargo proceso creado exitosamente con ID: {}", savedCargoProceso.getId());
        
        return cargoProcesoMapper.toResponseDTO(savedCargoProceso);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CargoProcesoResponseDTO> getCargoProcesoById(Long id) {
        log.debug("Buscando cargo proceso por ID: {}", id);
        return cargoProcesoRepository.findById(id)
                .map(cargoProcesoMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoProcesoResponseDTO> getAllCargosProceso() {
        log.debug("Obteniendo todos los cargos proceso");
        List<CargoProceso> cargosProceso = cargoProcesoRepository.findAll();
        return cargoProcesoMapper.toResponseDTOList(cargosProceso);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CargoProcesoResponseDTO> getAllCargosProcesoPaged(Pageable pageable) {
        log.debug("Obteniendo cargos proceso paginados");
        return cargoProcesoRepository.findAll(pageable)
                .map(cargoProcesoMapper::toResponseDTO);
    }
    
    @Override
    public CargoProcesoResponseDTO updateCargoProceso(Long id, CargoProcesoUpdateRequestDTO requestDTO) {
        log.info("Actualizando cargo proceso con ID: {}", id);
        
        CargoProceso cargoProceso = cargoProcesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo proceso no encontrado con ID: " + id));
        
        // Validar nombre único si se está actualizando
        if (requestDTO.getNombre() != null && 
            !cargoProceso.getNombre().equals(requestDTO.getNombre())) {
            
            if (cargoProcesoRepository.existsByProcesoIdAndNombre(
                    cargoProceso.getProceso().getId(), requestDTO.getNombre())) {
                throw new DuplicateResourceException(
                        String.format("Ya existe un cargo con nombre '%s' en el proceso '%s'", 
                                requestDTO.getNombre(), cargoProceso.getProceso().getNombre()));
            }
        }
        
        // Validar unidad si se actualiza
        Unidad nuevaUnidad = null;
        if (requestDTO.getUnidadId() != null && 
            !cargoProceso.getUnidad().getId().equals(requestDTO.getUnidadId())) {
            
            nuevaUnidad = unidadRepository.findById(requestDTO.getUnidadId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Unidad no encontrada con ID: " + requestDTO.getUnidadId()));
            
            if (!nuevaUnidad.getEstado()) {
                throw new BusinessException("No se puede asignar un cargo a una unidad inactiva");
            }
        }
        
        // Validar si se intenta activar un cargo en un proceso inactivo !cargoProceso.getActivo() &&
        if (requestDTO.getActivo() != null && requestDTO.getActivo() && 
             !cargoProceso.getProceso().getEstado()) {
            throw new BusinessException("No se puede activar un cargo en un proceso inactivo");
        }
        
        cargoProcesoMapper.updateEntity(requestDTO, cargoProceso, nuevaUnidad);
        CargoProceso updatedCargoProceso = cargoProcesoRepository.save(cargoProceso);
        log.info("Cargo proceso actualizado exitosamente con ID: {}", updatedCargoProceso.getId());
        
        return cargoProcesoMapper.toResponseDTO(updatedCargoProceso);
    }
    
    @Override
    public void deleteCargoProceso(Long id) {
        log.info("Eliminando cargo proceso con ID: {}", id);
        
        CargoProceso cargoProceso = cargoProcesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo proceso no encontrado con ID: " + id));
        
        // Validar si tiene historiales asociados
        if (cargoProceso.getHistoriales() != null && !cargoProceso.getHistoriales().isEmpty()) {
            throw new BusinessException("No se puede eliminar el cargo porque tiene historiales asociados");
        }
        
        cargoProcesoRepository.deleteById(id);
        log.info("Cargo proceso eliminado exitosamente con ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoProcesoResponseDTO> getCargosProcesoByProceso(Long procesoId) {
        log.debug("Buscando cargos proceso por proceso ID: {}", procesoId);
        
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado con ID: " + procesoId);
        }
        
        List<CargoProceso> cargosProceso = cargoProcesoRepository.findByProcesoIdOrderByNombreAsc(procesoId);
        return cargoProcesoMapper.toResponseDTOList(cargosProceso);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoProcesoResponseDTO> getCargosProcesoByUnidad(Long unidadId) {
        log.debug("Buscando cargos proceso por unidad ID: {}", unidadId);
        
        if (!unidadRepository.existsById(unidadId)) {
            throw new ResourceNotFoundException("Unidad no encontrada con ID: " + unidadId);
        }
        
        List<CargoProceso> cargosProceso = cargoProcesoRepository.findByUnidadIdOrderByNombreAsc(unidadId);
        return cargoProcesoMapper.toResponseDTOList(cargosProceso);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoProcesoResponseDTO> getCargosProcesoActivosByProceso(Long procesoId) {
        log.debug("Buscando cargos proceso activos por proceso ID: {}", procesoId);
        
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado con ID: " + procesoId);
        }
        //no funcionando aun por activos en luga solo por procesos
        List<CargoProceso> cargosProceso = cargoProcesoRepository.findByProcesoId(procesoId);
        return cargoProcesoMapper.toResponseDTOList(cargosProceso);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoProcesoResponseDTO> getCargosProcesoByProcesoAndUnidad(Long procesoId, Long unidadId) {
        log.debug("Buscando cargos proceso por proceso ID: {} y unidad ID: {}", procesoId, unidadId);
        
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado con ID: " + procesoId);
        }
        
        if (!unidadRepository.existsById(unidadId)) {
            throw new ResourceNotFoundException("Unidad no encontrada con ID: " + unidadId);
        }
        
        List<CargoProceso> cargosProceso = cargoProcesoRepository.findByProcesoIdAndUnidadId(procesoId, unidadId);
        return cargoProcesoMapper.toResponseDTOList(cargosProceso);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CargoProcesoResponseDTO> getCargoProcesoByProcesoAndNombre(Long procesoId, String nombre) {
        log.debug("Buscando cargo proceso por proceso ID: {} y nombre: {}", procesoId, nombre);
        return cargoProcesoRepository.findByProcesoIdAndNombre(procesoId, nombre)
                .map(cargoProcesoMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoProcesoResponseDTO> searchCargosProcesoByNombre(String nombre) {
        log.debug("Buscando cargos proceso que contengan: {}", nombre);
        List<CargoProceso> cargosProceso = cargoProcesoRepository.findByNombreContainingIgnoreCase(nombre);
        return cargoProcesoMapper.toResponseDTOList(cargosProceso);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoProcesoResponseDTO> searchCargosProceso(CargoProcesoSearchRequestDTO searchRequest) {
        log.debug("Buscando cargos proceso con filtros: {}", searchRequest);
        
        // Implementación básica - se puede mejorar con Specifications
        List<CargoProceso> cargosProceso = cargoProcesoRepository.findAll();
        
        return cargosProceso.stream()
                .filter(cp -> searchRequest.getProcesoId() == null || 
                        cp.getProceso().getId().equals(searchRequest.getProcesoId()))
                .filter(cp -> searchRequest.getUnidadId() == null || 
                        cp.getUnidad().getId().equals(searchRequest.getUnidadId()))
                .filter(cp -> searchRequest.getNombre() == null || 
                        cp.getNombre().toLowerCase().contains(searchRequest.getNombre().toLowerCase()))
                // .filter(cp -> searchRequest.getActivo() == null || 
                //         cp.getActivo().equals(searchRequest.getActivo()))
                .filter(cp -> searchRequest.getConHistoriales() == null || 
                        (searchRequest.getConHistoriales() ? 
                                (cp.getHistoriales() != null && !cp.getHistoriales().isEmpty()) :
                                (cp.getHistoriales() == null || cp.getHistoriales().isEmpty())))
                .map(cargoProcesoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<CargoProcesoResponseDTO> getCargoProcesoWithAllRelations(Long id) {
        log.debug("Buscando cargo proceso con todas las relaciones por ID: {}", id);
        return cargoProcesoRepository.findByIdWithAllRelations(id)
                .map(cargoProcesoMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoProcesoResponseDTO> getCargosProcesoByProcesoWithRelations(Long procesoId) {
        log.debug("Buscando cargos proceso con relaciones por proceso ID: {}", procesoId);
        
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado con ID: " + procesoId);
        }
        
        List<CargoProceso> cargosProceso = cargoProcesoRepository.findByProcesoIdWithRelations(procesoId);
        return cargoProcesoMapper.toResponseDTOList(cargosProceso);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CargoProcesoResponseDTO> getCargosProcesoWithHistorialCount(Long procesoId) {
        log.debug("Buscando cargos proceso con conteo de historiales por proceso ID: {}", procesoId);
        
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado con ID: " + procesoId);
        }
        
        List<Object[]> results = cargoProcesoRepository.findByProcesoIdWithHistorialCount(procesoId);
        
        return results.stream()
                .map(result -> {
                    CargoProceso cp = (CargoProceso) result[0];
                    Long totalHistoriales = (Long) result[1];
                    CargoProcesoResponseDTO dto = cargoProcesoMapper.toResponseDTO(cp);
                    // El total ya está en el DTO, pero podríamos actualizarlo si es necesario
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public CargoProcesoResponseDTO activarCargoProceso(Long id) {
        log.info("Activando cargo proceso con ID: {}", id);
        
        CargoProceso cargoProceso = cargoProcesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo proceso no encontrado con ID: " + id));
        
        // if (cargoProceso.getActivo()) {
        //     throw new BusinessException("El cargo proceso ya está activo");
        // }
        
        // Validar que el proceso esté activo
        if (!cargoProceso.getProceso().getEstado()) {
            throw new BusinessException("No se puede activar un cargo en un proceso inactivo");
        }
        
        //cargoProceso.setActivo(true);
        CargoProceso updatedCargoProceso = cargoProcesoRepository.save(cargoProceso);
        log.info("Cargo proceso activado exitosamente con ID: {}", id);
        
        return cargoProcesoMapper.toResponseDTO(updatedCargoProceso);
    }
    
    @Override
    public CargoProcesoResponseDTO desactivarCargoProceso(Long id) {
        log.info("Desactivando cargo proceso con ID: {}", id);
        
        CargoProceso cargoProceso = cargoProcesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo proceso no encontrado con ID: " + id));
        
        // if (!cargoProceso.getActivo()) {
        //     throw new BusinessException("El cargo proceso ya está inactivo");
        // }
        
        // Validar si tiene historiales activos
        if (tieneHistorialesActivos(id)) {
            throw new BusinessException("No se puede desactivar el cargo porque tiene historiales activos");
        }
        
        //cargoProceso.setActivo(false);
        CargoProceso updatedCargoProceso = cargoProcesoRepository.save(cargoProceso);
        log.info("Cargo proceso desactivado exitosamente con ID: {}", id);
        
        return cargoProcesoMapper.toResponseDTO(updatedCargoProceso);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean tieneHistorialesActivos(Long id) {
        log.debug("Verificando si cargo proceso ID: {} tiene historiales activos", id);
        
        return cargoProcesoRepository.findById(id)
                .map(cp -> cp.getHistoriales() != null && 
                        cp.getHistoriales().stream().anyMatch(h -> h.getActivo()))
                .orElse(false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countCargosProcesoByProceso(Long procesoId) {
        log.debug("Contando cargos proceso por proceso ID: {}", procesoId);
        
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado con ID: " + procesoId);
        }
        
        return cargoProcesoRepository.countByProcesoId(procesoId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countCargosProcesoByUnidad(Long unidadId) {
        log.debug("Contando cargos proceso por unidad ID: {}", unidadId);
        
        if (!unidadRepository.existsById(unidadId)) {
            throw new ResourceNotFoundException("Unidad no encontrada con ID: " + unidadId);
        }
        
        return cargoProcesoRepository.countByUnidadId(unidadId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsCargoProcesoInProceso(Long procesoId, String nombre) {
        log.debug("Verificando existencia de cargo '{}' en proceso ID: {}", nombre, procesoId);
        return cargoProcesoRepository.existsByProcesoIdAndNombre(procesoId, nombre);
    }
    @Override
    public List<CargoProcesoResponseDTO> createCargosSimple(List<CargoProcesoCreateRequestDTO> cargos) {
    List<CargoProcesoResponseDTO> responses = new ArrayList<>();
    
    for (CargoProcesoCreateRequestDTO dto : cargos) {
        CargoProcesoResponseDTO response = createCargoProceso(dto);
        
        responses.add(response);
    }
    
    return responses;
}
}