package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoCreateRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoSearchRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoUpdateRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoResponseDTO;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.exception.DuplicateResourceException;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoMapper;
import com.credenciales.tribunal.model.entity.CargoProceso;
import com.credenciales.tribunal.model.entity.HistorialCargoProceso;
import com.credenciales.tribunal.model.entity.Personal;
import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import com.credenciales.tribunal.repository.CargoProcesoRepository;
import com.credenciales.tribunal.repository.HistorialCargoProcesoRepository;
import com.credenciales.tribunal.repository.PersonalRepository;
import com.credenciales.tribunal.repository.ProcesoElectoralRepository;
import com.credenciales.tribunal.service.HistorialCargoProcesoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class HistorialCargoProcesoServiceImpl implements HistorialCargoProcesoService {
    
    private final HistorialCargoProcesoRepository historialRepository;
    private final CargoProcesoRepository cargoProcesoRepository;
    private final PersonalRepository personalRepository;
    private final ProcesoElectoralRepository procesoRepository;
    private final HistorialCargoProcesoMapper historialMapper;
    
    @Override
    public HistorialCargoProcesoResponseDTO createHistorial(HistorialCargoProcesoCreateRequestDTO requestDTO) {
        log.info("Creando nuevo historial de cargo proceso para personal ID: {} y cargo proceso ID: {}", 
                requestDTO.getPersonalId(), requestDTO.getCargoProcesoId());
        
        // Validar que el cargo proceso exista y esté activo
        CargoProceso cargoProceso = cargoProcesoRepository.findById(requestDTO.getCargoProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo proceso no encontrado con ID: " + requestDTO.getCargoProcesoId()));
        
        // if (!cargoProceso.getActivo()) {
        //     throw new BusinessException("No se puede asignar personal a un cargo proceso inactivo");
        // }
        
        // Validar que el proceso del cargo esté activo y vigente
        ProcesoElectoral proceso = cargoProceso.getProceso();
        if (!proceso.getEstado()) {
            throw new BusinessException("No se puede asignar personal a un cargo de un proceso inactivo");
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isBefore(proceso.getFechaInicio().atStartOfDay()) || 
            ahora.isAfter(proceso.getFechaFin().atStartOfDay())) {
            throw new BusinessException("No se puede asignar personal a un cargo fuera del período del proceso electoral");
        }
        
        // Validar que el personal exista
        Personal personal = personalRepository.findById(requestDTO.getPersonalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personal no encontrado con ID: " + requestDTO.getPersonalId()));
        
        // Validar que el personal no tenga un historial activo en el mismo cargo proceso
        if (historialRepository.existsByPersonalIdAndCargoProcesoIdAndActivoTrue(
                requestDTO.getPersonalId(), requestDTO.getCargoProcesoId())) {
            throw new DuplicateResourceException(
                    String.format("El personal ya tiene un historial activo en el cargo '%s'", 
                            cargoProceso.getNombre()));
        }
        
        // Validar que el personal pueda ser asignado (no tenga otro historial activo en otro cargo del mismo proceso)
        if (!puedeAsignarPersonalACargoProceso(requestDTO.getPersonalId(), requestDTO.getCargoProcesoId())) {
            throw new BusinessException(
                    "El personal ya tiene un historial activo en otro cargo del mismo proceso");
        }
        
        // Validar fechas
        validarFechas(requestDTO.getFechaInicio(), requestDTO.getFechaFin(), proceso);
        
        // Si el historial se crea como inactivo, debe tener fecha de fin
        if (!requestDTO.getActivo() && requestDTO.getFechaFin() == null) {
            throw new BusinessException("Si el historial se crea como inactivo, debe tener fecha de fin");
        }
        
        HistorialCargoProceso historial = historialMapper.toEntity(requestDTO, cargoProceso, personal);
        HistorialCargoProceso savedHistorial = historialRepository.save(historial);
        log.info("Historial de cargo proceso creado exitosamente con ID: {}", savedHistorial.getId());
        
        return historialMapper.toResponseDTO(savedHistorial);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<HistorialCargoProcesoResponseDTO> getHistorialById(Long id) {
        log.debug("Buscando historial de cargo proceso por ID: {}", id);
        return historialRepository.findById(id)
                .map(historialMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getAllHistoriales() {
        log.debug("Obteniendo todos los historiales de cargo proceso");
        List<HistorialCargoProceso> historiales = historialRepository.findAll();
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<HistorialCargoProcesoResponseDTO> getAllHistorialesPaged(Pageable pageable) {
        log.debug("Obteniendo historiales de cargo proceso paginados");
        return historialRepository.findAll(pageable)
                .map(historialMapper::toResponseDTO);
    }
    
    @Override
    public HistorialCargoProcesoResponseDTO updateHistorial(Long id, HistorialCargoProcesoUpdateRequestDTO requestDTO) {
        log.info("Actualizando historial de cargo proceso con ID: {}", id);
        
        HistorialCargoProceso historial = historialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Historial de cargo proceso no encontrado con ID: " + id));
        
        // Validar fechas si se actualizan
        LocalDateTime nuevaFechaInicio = requestDTO.getFechaInicio() != null ? 
                requestDTO.getFechaInicio() : historial.getFechaInicio();
        LocalDateTime nuevaFechaFin = requestDTO.getFechaFin() != null ? 
                requestDTO.getFechaFin() : historial.getFechaFin();
        
        validarFechas(nuevaFechaInicio, nuevaFechaFin, historial.getCargoProceso().getProceso());
        
        // Validar consistencia de fechas
        if (nuevaFechaFin != null && nuevaFechaFin.isBefore(nuevaFechaInicio)) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        
        // Si se está desactivando y no tiene fecha fin, establecer fecha fin actual
        if (requestDTO.getActivo() != null && !requestDTO.getActivo() && 
            historial.getActivo() && historial.getFechaFin() == null) {
            requestDTO.setFechaFin(LocalDateTime.now());
        }
        
        historialMapper.updateEntity(requestDTO, historial);
        HistorialCargoProceso updatedHistorial = historialRepository.save(historial);
        log.info("Historial de cargo proceso actualizado exitosamente con ID: {}", updatedHistorial.getId());
        
        return historialMapper.toResponseDTO(updatedHistorial);
    }
    
    @Override
    public void deleteHistorial(Long id) {
        log.info("Eliminando historial de cargo proceso con ID: {}", id);
        
        if (!historialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Historial de cargo proceso no encontrado con ID: " + id);
        }
        
        historialRepository.deleteById(id);
        log.info("Historial de cargo proceso eliminado exitosamente con ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesByCargoProceso(Long cargoProcesoId) {
        log.debug("Buscando historiales por cargo proceso ID: {}", cargoProcesoId);
        
        if (!cargoProcesoRepository.existsById(cargoProcesoId)) {
            throw new ResourceNotFoundException("Cargo proceso no encontrado con ID: " + cargoProcesoId);
        }
        
        List<HistorialCargoProceso> historiales = historialRepository.findByCargoProcesoId(cargoProcesoId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesByPersonal(Long personalId) {
        log.debug("Buscando historiales por personal ID: {}", personalId);
        
        if (!personalRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("Personal no encontrado con ID: " + personalId);
        }
        
        List<HistorialCargoProceso> historiales = historialRepository.findByPersonalId(personalId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesByProceso(Long procesoId) {
        log.debug("Buscando historiales por proceso ID: {}", procesoId);
        
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado con ID: " + procesoId);
        }
        
        List<HistorialCargoProceso> historiales = historialRepository.findByProcesoId(procesoId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesByUnidad(Long unidadId) {
        log.debug("Buscando historiales por unidad ID: {}", unidadId);
        
        List<HistorialCargoProceso> historiales = historialRepository.findByUnidadId(unidadId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesActivosByCargoProceso(Long cargoProcesoId) {
        log.debug("Buscando historiales activos por cargo proceso ID: {}", cargoProcesoId);
        
        if (!cargoProcesoRepository.existsById(cargoProcesoId)) {
            throw new ResourceNotFoundException("Cargo proceso no encontrado con ID: " + cargoProcesoId);
        }
        
        List<HistorialCargoProceso> historiales = historialRepository.findByCargoProcesoIdAndActivoTrue(cargoProcesoId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesActivosByPersonal(Long personalId) {
        log.debug("Buscando historiales activos por personal ID: {}", personalId);
        
        if (!personalRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("Personal no encontrado con ID: " + personalId);
        }
        
        List<HistorialCargoProceso> historiales = historialRepository.findByPersonalIdAndActivoTrue(personalId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesActivosByProceso(Long procesoId) {
        log.debug("Buscando historiales activos por proceso ID: {}", procesoId);
        
        if (!procesoRepository.existsById(procesoId)) {
            throw new ResourceNotFoundException("Proceso no encontrado con ID: " + procesoId);
        }
        
        List<HistorialCargoProceso> historiales = historialRepository.findActivosByProcesoId(procesoId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<HistorialCargoProcesoResponseDTO> getHistorialActivoByPersonalAndCargoProceso(
            Long personalId, Long cargoProcesoId) {
        log.debug("Buscando historial activo para personal ID: {} y cargo proceso ID: {}", 
                personalId, cargoProcesoId);
        return historialRepository.findByPersonalIdAndCargoProcesoIdAndActivoTrue(personalId, cargoProcesoId)
                .map(historialMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesByRangoFechas(
            LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Buscando historiales entre {} y {}", fechaInicio, fechaFin);
        List<HistorialCargoProceso> historiales = historialRepository.findByFechaInicioBetween(fechaInicio, fechaFin);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesFinalizadosEnRango(
            LocalDateTime start, LocalDateTime end) {
        log.debug("Buscando historiales finalizados entre {} y {}", start, end);
        List<HistorialCargoProceso> historiales = historialRepository.findByFechaFinBetween(start, end);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesSinFechaFin() {
        log.debug("Buscando historiales sin fecha fin (activos)");
        List<HistorialCargoProceso> historiales = historialRepository.findByFechaFinIsNullAndActivoTrue();
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> searchHistoriales(
            HistorialCargoProcesoSearchRequestDTO searchRequest) {
        log.debug("Buscando historiales con filtros: {}", searchRequest);
        
        // Implementación básica - se puede mejorar con Specifications
        List<HistorialCargoProceso> historiales = historialRepository.findAll();
        
        LocalDateTime ahora = LocalDateTime.now();
        
        return historiales.stream()
                .filter(h -> searchRequest.getCargoProcesoId() == null || 
                        h.getCargoProceso().getId().equals(searchRequest.getCargoProcesoId()))
                .filter(h -> searchRequest.getPersonalId() == null || 
                        h.getPersonal().getId().equals(searchRequest.getPersonalId()))
                .filter(h -> searchRequest.getProcesoId() == null || 
                        (h.getCargoProceso().getProceso() != null && 
                         h.getCargoProceso().getProceso().getId().equals(searchRequest.getProcesoId())))
                .filter(h -> searchRequest.getUnidadId() == null || 
                        (h.getCargoProceso().getUnidad() != null && 
                         h.getCargoProceso().getUnidad().getId().equals(searchRequest.getUnidadId())))
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
                .filter(h -> searchRequest.getSinFechaFin() == null || 
                        (searchRequest.getSinFechaFin() ? h.getFechaFin() == null : h.getFechaFin() != null))
                .map(historialMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<HistorialCargoProcesoResponseDTO> getHistorialWithAllRelations(Long id) {
        log.debug("Buscando historial con todas las relaciones por ID: {}", id);
        return historialRepository.findByIdWithAllRelations(id)
                .map(historialMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesByCargoProcesoWithPersonal(Long cargoProcesoId) {
        log.debug("Buscando historiales con personal por cargo proceso ID: {}", cargoProcesoId);
        
        if (!cargoProcesoRepository.existsById(cargoProcesoId)) {
            throw new ResourceNotFoundException("Cargo proceso no encontrado con ID: " + cargoProcesoId);
        }
        
        List<HistorialCargoProceso> historiales = historialRepository.findByCargoProcesoIdWithPersonal(cargoProcesoId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getHistorialesByPersonalWithCargoProceso(Long personalId) {
        log.debug("Buscando historiales con cargo proceso por personal ID: {}", personalId);
        
        if (!personalRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("Personal no encontrado con ID: " + personalId);
        }
        
        List<HistorialCargoProceso> historiales = historialRepository.findByPersonalIdWithCargoProceso(personalId);
        return historialMapper.toResponseDTOList(historiales);
    }
    
    @Override
    public HistorialCargoProcesoResponseDTO finalizarHistorial(Long id, LocalDateTime fechaFin) {
        log.info("Finalizando historial de cargo proceso con ID: {}", id);
        
        HistorialCargoProceso historial = historialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Historial de cargo proceso no encontrado con ID: " + id));
        
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
        
        HistorialCargoProceso updatedHistorial = historialRepository.save(historial);
        log.info("Historial de cargo proceso finalizado exitosamente con ID: {}", id);
        
        return historialMapper.toResponseDTO(updatedHistorial);
    }
    
    @Override
    public HistorialCargoProcesoResponseDTO reactivarHistorial(Long id) {
        log.info("Reactivando historial de cargo proceso con ID: {}", id);
        
        HistorialCargoProceso historial = historialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Historial de cargo proceso no encontrado con ID: " + id));
        
        if (historial.getActivo()) {
            throw new BusinessException("El historial ya está activo");
        }
        
        // Verificar que el cargo proceso y el proceso sigan activos
        // if (!historial.getCargoProceso().getActivo()) {
        //     throw new BusinessException("No se puede reactivar el historial porque el cargo proceso está inactivo");
        // }
        
        ProcesoElectoral proceso = historial.getCargoProceso().getProceso();
        if (!proceso.getEstado()) {
            throw new BusinessException("No se puede reactivar el historial porque el proceso electoral está inactivo");
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isBefore(proceso.getFechaInicio().atStartOfDay()) || 
            ahora.isAfter(proceso.getFechaFin().atStartOfDay())) {
            throw new BusinessException("No se puede reactivar el historial porque el proceso electoral no está vigente");
        }
        
        // Verificar que no tenga otro historial activo en el mismo cargo proceso
        if (historialRepository.existsByPersonalIdAndCargoProcesoIdAndActivoTrue(
                historial.getPersonal().getId(), historial.getCargoProceso().getId())) {
            throw new BusinessException("El personal ya tiene un historial activo en este cargo proceso");
        }
        
        historial.setActivo(true);
        historial.setFechaFin(null);
        
        HistorialCargoProceso updatedHistorial = historialRepository.save(historial);
        log.info("Historial de cargo proceso reactivado exitosamente con ID: {}", id);
        
        return historialMapper.toResponseDTO(updatedHistorial);
    }
    
    @Override
    public HistorialCargoProcesoResponseDTO asignarPersonalACargoProceso(
            Long personalId, Long cargoProcesoId, LocalDateTime fechaInicio) {
        log.info("Asignando personal ID: {} a cargo proceso ID: {}", personalId, cargoProcesoId);
        
        HistorialCargoProcesoCreateRequestDTO requestDTO = HistorialCargoProcesoCreateRequestDTO.builder()
                .personalId(personalId)
                .cargoProcesoId(cargoProcesoId)
                .fechaInicio(fechaInicio != null ? fechaInicio : LocalDateTime.now())
                .activo(true)
                .build();
        
        return createHistorial(requestDTO);
    }
    
    @Override
    public HistorialCargoProcesoResponseDTO reasignarPersonal(
            Long historialId, Long nuevoCargoProcesoId, LocalDateTime fechaReasignacion) {
        log.info("Reasignando historial ID: {} a nuevo cargo proceso ID: {}", historialId, nuevoCargoProcesoId);
        
        HistorialCargoProceso historialActual = historialRepository.findById(historialId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Historial de cargo proceso no encontrado con ID: " + historialId));
        
        if (!historialActual.getActivo()) {
            throw new BusinessException("No se puede reasignar un historial inactivo");
        }
        
        // Finalizar el historial actual
        LocalDateTime fechaFin = fechaReasignacion != null ? 
                fechaReasignacion.minusSeconds(1) : LocalDateTime.now().minusSeconds(1);
        finalizarHistorial(historialId, fechaFin);
        
        // Crear nuevo historial en el nuevo cargo proceso
        return asignarPersonalACargoProceso(
                historialActual.getPersonal().getId(), 
                nuevoCargoProcesoId, 
                fechaReasignacion != null ? fechaReasignacion : LocalDateTime.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean tieneHistorialActivoEnCargoProceso(Long personalId, Long cargoProcesoId) {
        log.debug("Verificando si personal ID: {} tiene historial activo en cargo proceso ID: {}", 
                personalId, cargoProcesoId);
        return historialRepository.existsByPersonalIdAndCargoProcesoIdAndActivoTrue(personalId, cargoProcesoId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean puedeAsignarPersonalACargoProceso(Long personalId, Long cargoProcesoId) {
        log.debug("Verificando si personal ID: {} puede ser asignado a cargo proceso ID: {}", 
                personalId, cargoProcesoId);
        
        CargoProceso nuevoCargo = cargoProcesoRepository.findById(cargoProcesoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo proceso no encontrado con ID: " + cargoProcesoId));
        
        // Obtener todos los historiales activos del personal
        List<HistorialCargoProceso> historialesActivos = 
                historialRepository.findByPersonalIdAndActivoTrue(personalId);
        
        // Verificar que no tenga un historial activo en el mismo proceso
        return historialesActivos.stream()
                .noneMatch(h -> h.getCargoProceso().getProceso().getId()
                        .equals(nuevoCargo.getProceso().getId()));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countHistorialesActivosByCargoProceso(Long cargoProcesoId) {
        log.debug("Contando historiales activos por cargo proceso ID: {}", cargoProcesoId);
        return historialRepository.countByCargoProcesoIdAndActivoTrue(cargoProcesoId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countHistorialesActivosByPersonal(Long personalId) {
        log.debug("Contando historiales activos por personal ID: {}", personalId);
        return historialRepository.countByPersonalIdAndActivoTrue(personalId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countHistorialesActivosByProceso(Long procesoId) {
        log.debug("Contando historiales activos por proceso ID: {}", procesoId);
        return historialRepository.countActivosByProcesoId(procesoId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoProcesoResponseDTO> getUltimosHistorialesByPersonal(
            Long personalId, int limite) {
        log.debug("Obteniendo últimos {} historiales del personal ID: {}", limite, personalId);
        
        Pageable pageable = PageRequest.of(0, limite);
        List<HistorialCargoProceso> historiales = 
                historialRepository.findLatestByPersonalId(personalId, pageable);
        
        return historialMapper.toResponseDTOList(historiales);
    }
    
    // Métodos privados de validación
    private void validarFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin, 
                               ProcesoElectoral proceso) {
        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        
        // Validar que las fechas estén dentro del período del proceso
        LocalDateTime inicioProceso = proceso.getFechaInicio().atStartOfDay();
        LocalDateTime finProceso = proceso.getFechaFin().atStartOfDay();
        
        if (fechaInicio.isBefore(inicioProceso) || 
            (fechaFin != null && fechaFin.isAfter(finProceso))) {
            throw new BusinessException(
                    "Las fechas del historial deben estar dentro del período del proceso electoral");
        }
    }
}