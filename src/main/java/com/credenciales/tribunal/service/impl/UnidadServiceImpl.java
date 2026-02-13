package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.unidad.UnidadRequestDTO;
import com.credenciales.tribunal.dto.unidad.UnidadResponseDTO;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.exception.DuplicateResourceException;
import com.credenciales.tribunal.dto.unidad.UnidadMapper;
import com.credenciales.tribunal.model.entity.Unidad;
import com.credenciales.tribunal.repository.UnidadRepository;
import com.credenciales.tribunal.service.UnidadService;
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
public class UnidadServiceImpl implements UnidadService {
    
    private final UnidadRepository unidadRepository;
    private final UnidadMapper unidadMapper;
    
    @Override
    public UnidadResponseDTO createUnidad(UnidadRequestDTO requestDTO) {
        log.info("Creando nueva unidad: {}", requestDTO.getNombre());
        
        // Validar duplicados
        if (unidadRepository.existsByNombre(requestDTO.getNombre())) {
            throw new DuplicateResourceException("Ya existe una unidad con el nombre: " + requestDTO.getNombre());
        }
        
        if (unidadRepository.existsByAbreviatura(requestDTO.getAbreviatura())) {
            throw new DuplicateResourceException("Ya existe una unidad con la abreviatura: " + requestDTO.getAbreviatura());
        }
        
        Unidad unidad = unidadMapper.toEntity(requestDTO);
        Unidad savedUnidad = unidadRepository.save(unidad);
        log.info("Unidad creada exitosamente con ID: {}", savedUnidad.getId());
        
        return unidadMapper.toResponseDTO(savedUnidad);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UnidadResponseDTO> getUnidadById(Long id) {
        log.debug("Buscando unidad por ID: {}", id);
        return unidadRepository.findById(id)
                .map(unidadMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UnidadResponseDTO> getAllUnidades() {
        log.debug("Obteniendo todas las unidades");
        List<Unidad> unidades = unidadRepository.findAll();
        return unidadMapper.toResponseDTOList(unidades);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UnidadResponseDTO> getAllUnidadesPaged(Pageable pageable) {
        log.debug("Obteniendo unidades paginadas");
        return unidadRepository.findAll(pageable)
                .map(unidadMapper::toResponseDTO);
    }
    
    @Override
    public UnidadResponseDTO updateUnidad(Long id, UnidadRequestDTO requestDTO) {
        log.info("Actualizando unidad con ID: {}", id);
        
        Unidad unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada con ID: " + id));
        
        // Validar duplicados si el nombre cambió
        if (!unidad.getNombre().equals(requestDTO.getNombre()) && 
            unidadRepository.existsByNombre(requestDTO.getNombre())) {
            throw new DuplicateResourceException("Ya existe una unidad con el nombre: " + requestDTO.getNombre());
        }
        
        // Validar duplicados si la abreviatura cambió
        if (!unidad.getAbreviatura().equals(requestDTO.getAbreviatura()) && 
            unidadRepository.existsByAbreviatura(requestDTO.getAbreviatura())) {
            throw new DuplicateResourceException("Ya existe una unidad con la abreviatura: " + requestDTO.getAbreviatura());
        }
        
        unidadMapper.updateEntity(requestDTO, unidad);
        Unidad updatedUnidad = unidadRepository.save(unidad);
        log.info("Unidad actualizada exitosamente con ID: {}", updatedUnidad.getId());
        
        return unidadMapper.toResponseDTO(updatedUnidad);
    }
    
    @Override
    public void deleteUnidad(Long id) {
        log.info("Eliminando unidad con ID: {}", id);
        
        if (!unidadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Unidad no encontrada con ID: " + id);
        }
        
        unidadRepository.deleteById(id);
        log.info("Unidad eliminada exitosamente con ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UnidadResponseDTO> getUnidadesByEstado(Boolean estado) {
        log.debug("Buscando unidades por estado: {}", estado);
        List<Unidad> unidades = unidadRepository.findByEstado(estado);
        return unidadMapper.toResponseDTOList(unidades);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UnidadResponseDTO> getUnidadByNombre(String nombre) {
        log.debug("Buscando unidad por nombre: {}", nombre);
        return unidadRepository.findByNombre(nombre)
                .map(unidadMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UnidadResponseDTO> getUnidadByAbreviatura(String abreviatura) {
        log.debug("Buscando unidad por abreviatura: {}", abreviatura);
        return unidadRepository.findByAbreviatura(abreviatura)
                .map(unidadMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UnidadResponseDTO> searchUnidadesByNombre(String nombre) {
        log.debug("Buscando unidades que contengan: {}", nombre);
        List<Unidad> unidades = unidadRepository.findByNombreContainingIgnoreCase(nombre);
        return unidadMapper.toResponseDTOList(unidades);
    }
    
    @Override
    public UnidadResponseDTO cambiarEstadoUnidad(Long id, Boolean estado) {
        log.info("Cambiando estado de unidad ID: {} a: {}", id, estado);
        
        Unidad unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada con ID: " + id));
        
        unidad.setEstado(estado);
        Unidad updatedUnidad = unidadRepository.save(unidad);
        
        return unidadMapper.toResponseDTO(updatedUnidad);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UnidadResponseDTO> getUnidadWithCargos(Long id) {
        log.debug("Buscando unidad con cargos por ID: {}", id);
        return unidadRepository.findByIdWithCargos(id)
                .map(unidadMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UnidadResponseDTO> getUnidadWithCargosProceso(Long id) {
        log.debug("Buscando unidad con cargos en proceso por ID: {}", id);
        return unidadRepository.findByIdWithCargosProceso(id)
                .map(unidadMapper::toResponseDTO);
    }
}