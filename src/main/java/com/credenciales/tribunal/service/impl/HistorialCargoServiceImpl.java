package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.historialcargo.HistorialCargoCreateRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoSearchRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoUpdateRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoPatchRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoResponseDTO;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoResponseDTO;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.exception.DuplicateResourceException;
import com.credenciales.tribunal.dto.historialcargo.HistorialCargoMapper;
import com.credenciales.tribunal.model.entity.Cargo;
import com.credenciales.tribunal.model.entity.CargoProceso;
import com.credenciales.tribunal.model.entity.HistorialCargo;
import com.credenciales.tribunal.model.entity.HistorialCargoProceso;
import com.credenciales.tribunal.model.entity.Personal;
import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import com.credenciales.tribunal.repository.CargoRepository;
import com.credenciales.tribunal.repository.HistorialCargoRepository;
import com.credenciales.tribunal.repository.PersonalRepository;
import com.credenciales.tribunal.service.HistorialCargoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HistorialCargoServiceImpl implements HistorialCargoService {
    
    private final HistorialCargoRepository historialRepository;
    private final PersonalRepository personalRepository;
    private final CargoRepository cargoRepository;
    private final HistorialCargoMapper historialMapper;
    
    @Override
    public HistorialCargoResponseDTO createHistorial(HistorialCargoCreateRequestDTO requestDTO) {
        log.info("Creando nuevo historial de cargo para personal ID: {} y cargo ID: {}", 
                requestDTO.getPersonalId(), requestDTO.getCargoId());
        
        // Validar que el personal exista
        Personal personal = personalRepository.findById(requestDTO.getPersonalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personal no encontrado con ID: " + requestDTO.getPersonalId()));
        
        // Validar que el cargo exista
        Cargo cargo = cargoRepository.findById(requestDTO.getCargoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo no encontrado con ID: " + requestDTO.getCargoId()));
        
        // Validar que el personal no tenga un historial activo en el mismo cargo
        if (historialRepository.existsByPersonalIdAndCargoIdAndActivoTrue(
                requestDTO.getPersonalId(), requestDTO.getCargoId())) {
            throw new DuplicateResourceException(
                    String.format("El personal ya tiene un historial activo en el cargo '%s'", 
                            cargo.getNombre()));
        }
        
        // Validar fechas
        if (requestDTO.getFechaFin() != null && 
            requestDTO.getFechaFin().isBefore(requestDTO.getFechaInicio())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        
        // Si el historial se crea como inactivo, debe tener fecha de fin
        if (!requestDTO.getActivo() && requestDTO.getFechaFin() == null) {
            throw new BusinessException("Si el historial se crea como inactivo, debe tener fecha de fin");
        }
        
        HistorialCargo historial = historialMapper.toEntity(requestDTO, personal, cargo);
        HistorialCargo savedHistorial = historialRepository.save(historial);
        log.info("Historial de cargo creado exitosamente con ID: {}", savedHistorial.getId());
        
        return historialMapper.toResponseDTO(savedHistorial);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<HistorialCargoResponseDTO> getHistorialById(Long id) {
        log.debug("Buscando historial de cargo por ID: {}", id);
        return historialRepository.findById(id)
                .map(historialMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoResponseDTO> getAllHistoriales() {
        log.debug("Obteniendo todos los historiales de cargo");
        List<HistorialCargo> historiales = historialRepository.findAll();
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<HistorialCargoResponseDTO> getAllHistorialesPaged(Pageable pageable) {
        log.debug("Obteniendo historiales de cargo paginados");
        return historialRepository.findAll(pageable)
                .map(historialMapper::toResponseDTO);
    }
    
    @Override
    public HistorialCargoResponseDTO updateHistorial(Long id, HistorialCargoUpdateRequestDTO requestDTO) {
        log.info("Actualizando historial de cargo con ID: {}", id);
        
        HistorialCargo historial = historialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Historial de cargo no encontrado con ID: " + id));
        
        // Validar fechas si se actualizan
        if (requestDTO.getFechaInicio() != null && requestDTO.getFechaFin() != null) {
            if (requestDTO.getFechaFin().isBefore(requestDTO.getFechaInicio())) {
                throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio");
            }
        } else if (requestDTO.getFechaInicio() != null && historial.getFechaFin() != null) {
            if (historial.getFechaFin().isBefore(requestDTO.getFechaInicio())) {
                throw new BusinessException("La fecha de fin no puede ser anterior a la nueva fecha de inicio");
            }
        } else if (requestDTO.getFechaFin() != null && historial.getFechaInicio() != null) {
            if (requestDTO.getFechaFin().isBefore(historial.getFechaInicio())) {
                throw new BusinessException("La nueva fecha de fin no puede ser anterior a la fecha de inicio");
            }
        }
        
        historialMapper.updateEntity(requestDTO, historial);
        HistorialCargo updatedHistorial = historialRepository.save(historial);
        log.info("Historial de cargo actualizado exitosamente con ID: {}", updatedHistorial.getId());
        
        return historialMapper.toResponseDTO(updatedHistorial);
    }
/*     @Override
public HistorialCargoProcesoResponseDTO reasignarCargoHistorial(Long id, HistorialCargoProcesoPatchRequestDTO requestDTO) {
    log.info("Reasignando cargo en historial ID: {} al nuevo cargo ID: {}", id, requestDTO.getIdCargo());
    
    // 1. Buscar el historial existente
    HistorialCargoProceso historial = historialRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Historial de cargo proceso no encontrado con ID: " + id));
    
    // 2. Validar que el nuevo cargo sea diferente al actual
    if (requestDTO.getIdCargo().equals(historial.getCargoProceso().getId())) {
        throw new BusinessException("El cargo seleccionado es el mismo que el actual");
    }
    
    // 3. Buscar y validar el nuevo cargo proceso
    CargoProceso nuevoCargoProceso = cargoProcesoRepository.findById(requestDTO.getIdCargo())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Cargo proceso no encontrado con ID: " + requestDTO.getIdCargo()));
    
    // 4. Validar que el proceso del nuevo cargo esté activo
    ProcesoElectoral nuevoProceso = nuevoCargoProceso.getProceso();
    if (!nuevoProceso.getEstado()) {
        throw new BusinessException("No se puede reasignar a un cargo de un proceso inactivo");
    }
    
    // 5. Validar vigencia del nuevo proceso
    LocalDateTime ahora = LocalDateTime.now();
    if (ahora.isBefore(nuevoProceso.getFechaInicio().atStartOfDay()) || 
        ahora.isAfter(nuevoProceso.getFechaFin().atStartOfDay())) {
        throw new BusinessException("El proceso electoral del nuevo cargo no está vigente");
    }
    
    // 6. Validar que el personal no tenga otro historial activo en el nuevo proceso
    List<HistorialCargoProceso> activosDelPersonal = historialRepository
            .findByPersonalIdAndActivoTrue(historial.getPersonal().getId());
    
    boolean tieneOtroActivoEnMismoProceso = activosDelPersonal.stream()
            .filter(h -> !h.getId().equals(id))  // excluir este historial
            .anyMatch(h -> h.getCargoProceso().getProceso().getId()
                    .equals(nuevoProceso.getId()));
    
    if (tieneOtroActivoEnMismoProceso) {
        throw new BusinessException(
                "El personal ya tiene un historial activo en otro cargo del mismo proceso");
    }
    
    // 7. Validar que no tenga un historial activo en el nuevo cargo específico
    boolean yaTieneActivoEnNuevoCargo = historialRepository
            .existsByPersonalIdAndCargoProcesoIdAndActivoTrue(
                    historial.getPersonal().getId(), requestDTO.getIdCargo());
    
    if (yaTieneActivoEnNuevoCargo) {
        throw new DuplicateResourceException(
                String.format("El personal ya tiene un historial activo en el cargo '%s'", 
                        nuevoCargoProceso.getNombre()));
    }
    
    // 8. Validar fechas (si vienen en el request)
    LocalDateTime fechaInicioValidar = requestDTO.getFechaInicio() != null ? 
            requestDTO.getFechaInicio() : historial.getFechaInicio();
    LocalDateTime fechaFinValidar = requestDTO.getFechaFin() != null ? 
            requestDTO.getFechaFin() : historial.getFechaFin();
    
    validarFechas(fechaInicioValidar, fechaFinValidar, nuevoProceso);
    
    // 9. Si el historial está activo y no tiene fecha fin, pero se está desactivando
    if (requestDTO.getActivo() != null && !requestDTO.getActivo() && 
        historial.getActivo() && historial.getFechaFin() == null) {
        requestDTO.setFechaFin(LocalDateTime.now());
    }
    
    // 10. Actualizar el cargo proceso
    historial.setCargoProceso(nuevoCargoProceso);
    
    // 11. Actualizar fechas si vienen en el request
    if (requestDTO.getFechaInicio() != null) {
        historial.setFechaInicio(requestDTO.getFechaInicio());
    }
    if (requestDTO.getFechaFin() != null) {
        historial.setFechaFin(requestDTO.getFechaFin());
    }
    
    // 12. Actualizar activo si viene en el request
    if (requestDTO.getActivo() != null) {
        historial.setActivo(requestDTO.getActivo());
        // Si se desactiva y no tiene fecha fin, asignar ahora
        if (!requestDTO.getActivo() && historial.getFechaFin() == null) {
            historial.setFechaFin(LocalDateTime.now());
        }
    }
    
    // 13. Guardar cambios
    HistorialCargoProceso updatedHistorial = historialRepository.save(historial);
    log.info("Cargo reasignado exitosamente en historial ID: {} al nuevo cargo: {}", 
            id, nuevoCargoProceso.getNombre());
    
    return historialMapper.toResponseDTO(updatedHistorial);
} */
    
    @Override
    public void deleteHistorial(Long id) {
        log.info("Eliminando historial de cargo con ID: {}", id);
        
        if (!historialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Historial de cargo no encontrado con ID: " + id);
        }
        
        historialRepository.deleteById(id);
        log.info("Historial de cargo eliminado exitosamente con ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoResponseDTO> getHistorialesByPersonal(Long personalId) {
        log.debug("Buscando historiales por personal ID: {}", personalId);
        
        if (!personalRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("Personal no encontrado con ID: " + personalId);
        }
        
        List<HistorialCargo> historiales = historialRepository.findByPersonalId(personalId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoResponseDTO> getHistorialesByCargo(Long cargoId) {
        log.debug("Buscando historiales por cargo ID: {}", cargoId);
        
        if (!cargoRepository.existsById(cargoId)) {
            throw new ResourceNotFoundException("Cargo no encontrado con ID: " + cargoId);
        }
        
        List<HistorialCargo> historiales = historialRepository.findByCargoId(cargoId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoResponseDTO> getHistorialesActivosByPersonal(Long personalId) {
        log.debug("Buscando historiales activos por personal ID: {}", personalId);
        
        if (!personalRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("Personal no encontrado con ID: " + personalId);
        }
        
        List<HistorialCargo> historiales = historialRepository.findByPersonalIdAndActivoTrue(personalId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoResponseDTO> getHistorialesActivosByCargo(Long cargoId) {
        log.debug("Buscando historiales activos por cargo ID: {}", cargoId);
        
        if (!cargoRepository.existsById(cargoId)) {
            throw new ResourceNotFoundException("Cargo no encontrado con ID: " + cargoId);
        }
        
        List<HistorialCargo> historiales = historialRepository.findByCargoIdAndActivoTrue(cargoId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<HistorialCargoResponseDTO> getHistorialActivoByPersonalAndCargo(Long personalId, Long cargoId) {
        log.debug("Buscando historial activo para personal ID: {} y cargo ID: {}", personalId, cargoId);
        return historialRepository.findByPersonalIdAndCargoIdAndActivoTrue(personalId, cargoId)
                .map(historialMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoResponseDTO> getHistorialesByFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Buscando historiales entre {} y {}", fechaInicio, fechaFin);
        List<HistorialCargo> historiales = historialRepository.findByFechaInicioBetween(fechaInicio, fechaFin);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoResponseDTO> getHistorialesFinalizadosEnRango(LocalDateTime start, LocalDateTime end) {
        log.debug("Buscando historiales finalizados entre {} y {}", start, end);
        List<HistorialCargo> historiales = historialRepository.findByFechaFinBetween(start, end);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoResponseDTO> searchHistoriales(HistorialCargoSearchRequestDTO searchRequest) {
        log.debug("Buscando historiales con filtros: {}", searchRequest);
        
        // Implementación básica - se puede mejorar con Specifications
        List<HistorialCargo> historiales = historialRepository.findAll();
        
        // Aplicar filtros en memoria (considerar usar Specifications para mejor rendimiento)
        return historiales.stream()
                .filter(h -> searchRequest.getPersonalId() == null || 
                        h.getPersonal().getId().equals(searchRequest.getPersonalId()))
                .filter(h -> searchRequest.getCargoId() == null || 
                        h.getCargo().getId().equals(searchRequest.getCargoId()))
                .filter(h -> searchRequest.getActivo() == null || 
                        h.getActivo().equals(searchRequest.getActivo()))
                .filter(h -> searchRequest.getFechaInicioDesde() == null || 
                        !h.getFechaInicio().isBefore(searchRequest.getFechaInicioDesde()))
                .filter(h -> searchRequest.getFechaInicioHasta() == null || 
                        !h.getFechaInicio().isAfter(searchRequest.getFechaInicioHasta()))
                .filter(h -> searchRequest.getFechaFinDesde() == null || 
                        (h.getFechaFin() != null && !h.getFechaFin().isBefore(searchRequest.getFechaFinDesde())))
                .filter(h -> searchRequest.getFechaFinHasta() == null || 
                        (h.getFechaFin() != null && !h.getFechaFin().isAfter(searchRequest.getFechaFinHasta())))
                .map(historialMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public HistorialCargoResponseDTO finalizarHistorial(Long id, LocalDateTime fechaFin) {
        log.info("Finalizando historial de cargo con ID: {}", id);
        
        HistorialCargo historial = historialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Historial de cargo no encontrado con ID: " + id));
        
        if (!historial.getActivo()) {
            throw new BusinessException("El historial ya está finalizado");
        }
        
        if (fechaFin == null) {
            fechaFin = LocalDateTime.now();
        }
        
        if (fechaFin.isBefore(historial.getFechaInicio())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        
        historial.setFechaFin(fechaFin);
        historial.setActivo(false);
        
        HistorialCargo updatedHistorial = historialRepository.save(historial);
        log.info("Historial de cargo finalizado exitosamente con ID: {}", id);
        
        return historialMapper.toResponseDTO(updatedHistorial);
    }
    
    @Override
    public HistorialCargoResponseDTO reactivarHistorial(Long id) {
        log.info("Reactivando historial de cargo con ID: {}", id);
        
        HistorialCargo historial = historialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Historial de cargo no encontrado con ID: " + id));
        
        if (historial.getActivo()) {
            throw new BusinessException("El historial ya está activo");
        }
        
        // Verificar que no tenga otro historial activo en el mismo cargo
        if (historialRepository.existsByPersonalIdAndCargoIdAndActivoTrue(
                historial.getPersonal().getId(), historial.getCargo().getId())) {
            throw new BusinessException("El personal ya tiene un historial activo en este cargo");
        }
        
        historial.setActivo(true);
        historial.setFechaFin(null);
        
        HistorialCargo updatedHistorial = historialRepository.save(historial);
        log.info("Historial de cargo reactivado exitosamente con ID: {}", id);
        
        return historialMapper.toResponseDTO(updatedHistorial);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean tieneHistorialActivoEnCargo(Long personalId, Long cargoId) {
        log.debug("Verificando si personal ID: {} tiene historial activo en cargo ID: {}", personalId, cargoId);
        return historialRepository.existsByPersonalIdAndCargoIdAndActivoTrue(personalId, cargoId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countHistorialesActivosByPersonal(Long personalId) {
        log.debug("Contando historiales activos por personal ID: {}", personalId);
        return historialRepository.countByPersonalIdAndActivoTrue(personalId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countHistorialesActivosByCargo(Long cargoId) {
        log.debug("Contando historiales activos por cargo ID: {}", cargoId);
        return historialRepository.countByCargoIdAndActivoTrue(cargoId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<HistorialCargoResponseDTO> getHistorialWithAllRelations(Long id) {
        log.debug("Buscando historial con todas las relaciones por ID: {}", id);
        return historialRepository.findByIdWithRelations(id)
                .map(historialMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoResponseDTO> getHistorialesByPersonalWithDetails(Long personalId) {
        log.debug("Buscando historiales con detalles por personal ID: {}", personalId);
        
        if (!personalRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("Personal no encontrado con ID: " + personalId);
        }
        
        List<HistorialCargo> historiales = historialRepository.findByPersonalIdWithRelations(personalId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoResponseDTO> getHistorialesByCargoWithDetails(Long cargoId) {
        log.debug("Buscando historiales con detalles por cargo ID: {}", cargoId);
        
        if (!cargoRepository.existsById(cargoId)) {
            throw new ResourceNotFoundException("Cargo no encontrado con ID: " + cargoId);
        }
        
        List<HistorialCargo> historiales = historialRepository.findByCargoIdWithRelations(cargoId);
        return historialMapper.toResponseDTOList(historiales);
    }
}