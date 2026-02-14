package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralCreateRequestDTO;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralSearchRequestDTO;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralUpdateRequestDTO;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralResponseDTO;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.exception.DuplicateResourceException;
import com.credenciales.tribunal.dto.procesoelectoral.ProcesoElectoralMapper;
import com.credenciales.tribunal.model.entity.Imagen;
import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import com.credenciales.tribunal.repository.ImagenRepository;
import com.credenciales.tribunal.repository.ProcesoElectoralRepository;
import com.credenciales.tribunal.service.ProcesoElectoralService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProcesoElectoralServiceImpl implements ProcesoElectoralService {
    
    private final ProcesoElectoralRepository procesoRepository;
    private final ImagenRepository imagenRepository;
    private final ProcesoElectoralMapper procesoMapper;
    
    @Override
    public ProcesoElectoralResponseDTO createProceso(ProcesoElectoralCreateRequestDTO requestDTO) {
        log.info("Creando nuevo proceso electoral: {}", requestDTO.getNombre());
        
        // Validar que no exista un proceso con el mismo nombre
        if (procesoRepository.existsByNombre(requestDTO.getNombre())) {
            throw new DuplicateResourceException("Ya existe un proceso con el nombre: " + requestDTO.getNombre());
        }
        
        // Validar que la imagen exista
        Imagen imagen = imagenRepository.findById(requestDTO.getImagenId())
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + requestDTO.getImagenId()));
        
        // Validar fechas
        validarFechas(requestDTO.getFechaInicio(), requestDTO.getFechaFin());
        
        // Validar que no haya procesos activos que se sobrepongan si este proceso estará activo
        if (requestDTO.getEstado()) {
            validarSuperposicionProcesos(requestDTO.getFechaInicio(), requestDTO.getFechaFin(), null);
        }
        
        ProcesoElectoral proceso = procesoMapper.toEntity(requestDTO, imagen);
        ProcesoElectoral savedProceso = procesoRepository.save(proceso);
        log.info("Proceso electoral creado exitosamente con ID: {}", savedProceso.getId());
        
        return procesoMapper.toResponseDTO(savedProceso);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ProcesoElectoralResponseDTO> getProcesoById(Long id) {
        log.debug("Buscando proceso electoral por ID: {}", id);
        return procesoRepository.findById(id)
                .map(procesoMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProcesoElectoralResponseDTO> getAllProcesos() {
        log.debug("Obteniendo todos los procesos electorales");
        List<ProcesoElectoral> procesos = procesoRepository.findAll();
        return procesoMapper.toResponseDTOList(procesos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProcesoElectoralResponseDTO> getAllProcesosPaged(Pageable pageable) {
        log.debug("Obteniendo procesos electorales paginados");
        return procesoRepository.findAll(pageable)
                .map(procesoMapper::toResponseDTO);
    }
    
    @Override
    public ProcesoElectoralResponseDTO updateProceso(Long id, ProcesoElectoralUpdateRequestDTO requestDTO) {
        log.info("Actualizando proceso electoral con ID: {}", id);
        
        ProcesoElectoral proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado con ID: " + id));
        
        // Validar nombre único si se está actualizando
        if (requestDTO.getNombre() != null && !proceso.getNombre().equals(requestDTO.getNombre())) {
            if (procesoRepository.existsByNombre(requestDTO.getNombre())) {
                throw new DuplicateResourceException("Ya existe un proceso con el nombre: " + requestDTO.getNombre());
            }
        }
        
        // Validar imagen si se actualiza
        Imagen imagen = null;
        if (requestDTO.getImagenId() != null) {
            imagen = imagenRepository.findById(requestDTO.getImagenId())
                    .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + requestDTO.getImagenId()));
        }
        
        // Validar fechas si se actualizan
        LocalDate fechaInicio = requestDTO.getFechaInicio() != null ? 
                requestDTO.getFechaInicio() : proceso.getFechaInicio();
        LocalDate fechaFin = requestDTO.getFechaFin() != null ? 
                requestDTO.getFechaFin() : proceso.getFechaFin();
        
        validarFechas(fechaInicio, fechaFin);
        
        // Validar superposición si cambian fechas o estado
        Boolean nuevoEstado = requestDTO.getEstado() != null ? 
                requestDTO.getEstado() : proceso.getEstado();
        
        if (nuevoEstado && (requestDTO.getFechaInicio() != null || 
                            requestDTO.getFechaFin() != null || 
                            (requestDTO.getEstado() != null && !proceso.getEstado().equals(requestDTO.getEstado())))) {
            validarSuperposicionProcesos(fechaInicio, fechaFin, id);
        }
        
        procesoMapper.updateEntity(requestDTO, proceso, imagen);
        ProcesoElectoral updatedProceso = procesoRepository.save(proceso);
        log.info("Proceso electoral actualizado exitosamente con ID: {}", updatedProceso.getId());
        
        return procesoMapper.toResponseDTO(updatedProceso);
    }
    
    @Override
    public void deleteProceso(Long id) {
        log.info("Eliminando proceso electoral con ID: {}", id);
        
        ProcesoElectoral proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado con ID: " + id));
        
        // Validar si tiene cargos asociados
        if (proceso.getCargosProceso() != null && !proceso.getCargosProceso().isEmpty()) {
            throw new BusinessException("No se puede eliminar el proceso porque tiene cargos asociados");
        }
        
        procesoRepository.deleteById(id);
        log.info("Proceso electoral eliminado exitosamente con ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ProcesoElectoralResponseDTO> getProcesoByNombre(String nombre) {
        log.debug("Buscando proceso por nombre: {}", nombre);
        return procesoRepository.findByNombre(nombre)
                .map(procesoMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProcesoElectoralResponseDTO> searchProcesosByNombre(String nombre) {
        log.debug("Buscando procesos que contengan: {}", nombre);
        List<ProcesoElectoral> procesos = procesoRepository.findByNombreContainingIgnoreCase(nombre);
        return procesoMapper.toResponseDTOList(procesos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProcesoElectoralResponseDTO> getProcesosByEstado(Boolean estado) {
        log.debug("Buscando procesos por estado: {}", estado);
        List<ProcesoElectoral> procesos = procesoRepository.findByEstado(estado);
        return procesoMapper.toResponseDTOList(procesos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProcesoElectoralResponseDTO> getProcesosActivos() {
        log.debug("Buscando procesos activos");
        List<ProcesoElectoral> procesos = procesoRepository.findByEstadoTrueOrderByFechaInicioDesc();
        return procesoMapper.toResponseDTOList(procesos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProcesoElectoralResponseDTO> getProcesosByRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        log.debug("Buscando procesos entre {} y {}", fechaInicio, fechaFin);
        List<ProcesoElectoral> procesos = procesoRepository.findByFechaInicioBetween(fechaInicio, fechaFin);
        return procesoMapper.toResponseDTOList(procesos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProcesoElectoralResponseDTO> getProcesosVigentes(LocalDate fecha) {
        log.debug("Buscando procesos vigentes en fecha: {}", fecha);
        List<ProcesoElectoral> procesos = procesoRepository.findVigentesEnFecha(fecha);
        return procesoMapper.toResponseDTOList(procesos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProcesoElectoralResponseDTO> getProcesosVigentesActuales() {
        log.debug("Buscando procesos vigentes actualmente");
        List<ProcesoElectoral> procesos = procesoRepository.findActivosVigentesEnFecha(LocalDate.now());
        return procesoMapper.toResponseDTOList(procesos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProcesoElectoralResponseDTO> getProcesosProximos() {
        log.debug("Buscando procesos próximos a iniciar");
        List<ProcesoElectoral> procesos = procesoRepository.findProximosAIniciar(LocalDate.now());
        return procesoMapper.toResponseDTOList(procesos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProcesoElectoralResponseDTO> getProcesosFinalizados() {
        log.debug("Buscando procesos finalizados");
        List<ProcesoElectoral> procesos = procesoRepository.findFinalizados(LocalDate.now());
        return procesoMapper.toResponseDTOList(procesos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProcesoElectoralResponseDTO> searchProcesos(ProcesoElectoralSearchRequestDTO searchRequest) {
        log.debug("Buscando procesos con filtros: {}", searchRequest);
        
        // Implementación básica - se puede mejorar con Specifications
        List<ProcesoElectoral> procesos = procesoRepository.findAll();
        
        LocalDate hoy = LocalDate.now();
        
        return procesos.stream()
                .filter(p -> searchRequest.getNombre() == null || 
                        p.getNombre().toLowerCase().contains(searchRequest.getNombre().toLowerCase()))
                .filter(p -> searchRequest.getEstado() == null || 
                        p.getEstado().equals(searchRequest.getEstado()))
                .filter(p -> searchRequest.getFechaInicioDesde() == null || 
                        !p.getFechaInicio().isBefore(searchRequest.getFechaInicioDesde()))
                .filter(p -> searchRequest.getFechaInicioHasta() == null || 
                        !p.getFechaInicio().isAfter(searchRequest.getFechaInicioHasta()))
                .filter(p -> searchRequest.getFechaFinDesde() == null || 
                        !p.getFechaFin().isBefore(searchRequest.getFechaFinDesde()))
                .filter(p -> searchRequest.getFechaFinHasta() == null || 
                        !p.getFechaFin().isAfter(searchRequest.getFechaFinHasta()))
                .filter(p -> searchRequest.getVigente() == null || 
                        (searchRequest.getVigente() ? 
                                (hoy.isAfter(p.getFechaInicio()) && hoy.isBefore(p.getFechaFin()) && p.getEstado()) :
                                (!hoy.isAfter(p.getFechaInicio()) || !hoy.isBefore(p.getFechaFin()) || !p.getEstado())))
                .map(procesoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ProcesoElectoralResponseDTO> getProcesoWithImagen(Long id) {
        log.debug("Buscando proceso con imagen por ID: {}", id);
        return procesoRepository.findByIdWithImagen(id)
                .map(procesoMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ProcesoElectoralResponseDTO> getProcesoWithCargos(Long id) {
        log.debug("Buscando proceso con cargos por ID: {}", id);
        return procesoRepository.findByIdWithCargos(id)
                .map(procesoMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ProcesoElectoralResponseDTO> getProcesoWithAllRelations(Long id) {
        log.debug("Buscando proceso con todas las relaciones por ID: {}", id);
        return procesoRepository.findByIdWithAllRelations(id)
                .map(procesoMapper::toResponseDTO);
    }
    
    @Override
    public ProcesoElectoralResponseDTO activarProceso(Long id) {
        log.info("Activando proceso electoral con ID: {}", id);
        
        ProcesoElectoral proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado con ID: " + id));
        
        if (proceso.getEstado()) {
            throw new BusinessException("El proceso ya está activo");
        }
        
        // Validar superposición con otros procesos activos
        validarSuperposicionProcesos(proceso.getFechaInicio(), proceso.getFechaFin(), id);
        
        proceso.setEstado(true);
        ProcesoElectoral updatedProceso = procesoRepository.save(proceso);
        log.info("Proceso electoral activado exitosamente con ID: {}", id);
        
        return procesoMapper.toResponseDTO(updatedProceso);
    }
    
    @Override
    public ProcesoElectoralResponseDTO desactivarProceso(Long id) {
        log.info("Desactivando proceso electoral con ID: {}", id);
        
        ProcesoElectoral proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado con ID: " + id));
        
        if (!proceso.getEstado()) {
            throw new BusinessException("El proceso ya está inactivo");
        }
        
        proceso.setEstado(false);
        ProcesoElectoral updatedProceso = procesoRepository.save(proceso);
        log.info("Proceso electoral desactivado exitosamente con ID: {}", id);
        
        return procesoMapper.toResponseDTO(updatedProceso);
    }
    
    @Override
    public ProcesoElectoralResponseDTO extenderFechaFin(Long id, LocalDate nuevaFechaFin) {
        log.info("Extendiendo fecha fin del proceso ID: {} a {}", id, nuevaFechaFin);
        
        ProcesoElectoral proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proceso no encontrado con ID: " + id));
        
        if (nuevaFechaFin.isBefore(proceso.getFechaFin())) {
            throw new BusinessException("La nueva fecha fin no puede ser anterior a la fecha fin actual");
        }
        
        validarFechas(proceso.getFechaInicio(), nuevaFechaFin);
        
        // Validar superposición con otros procesos activos
        if (proceso.getEstado()) {
            validarSuperposicionProcesos(proceso.getFechaInicio(), nuevaFechaFin, id);
        }
        
        proceso.setFechaFin(nuevaFechaFin);
        ProcesoElectoral updatedProceso = procesoRepository.save(proceso);
        log.info("Fecha fin extendida exitosamente para proceso ID: {}", id);
        
        return procesoMapper.toResponseDTO(updatedProceso);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isProcesoVigente(Long id) {
        log.debug("Verificando si proceso ID: {} está vigente", id);
        
        return procesoRepository.findById(id)
                .map(p -> {
                    LocalDate hoy = LocalDate.now();
                    return p.getEstado() && 
                           !hoy.isBefore(p.getFechaInicio()) && 
                           !hoy.isAfter(p.getFechaFin());
                })
                .orElse(false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countProcesosByEstado(Boolean estado) {
        log.debug("Contando procesos por estado: {}", estado);
        return procesoRepository.countByEstado(estado);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ProcesoElectoralResponseDTO> getUltimoProceso() {
        log.debug("Buscando el último proceso electoral");
        return procesoRepository.findTopByOrderByFechaInicioDesc()
                .map(procesoMapper::toResponseDTO);
    }
    
    // Métodos privados de validación
    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaFin.isBefore(fechaInicio)) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        
        if (fechaInicio.isBefore(LocalDate.now().minusDays(1))) {
            log.warn("Creando/actualizando proceso con fecha inicio en el pasado: {}", fechaInicio);
        }
    }
    
    private void validarSuperposicionProcesos(LocalDate fechaInicio, LocalDate fechaFin, Long procesoIdExcluir) {
        List<ProcesoElectoral> procesosActivos = procesoRepository.findByEstado(true);
        
        for (ProcesoElectoral proceso : procesosActivos) {
            if (procesoIdExcluir != null && proceso.getId().equals(procesoIdExcluir)) {
                continue;
            }
            
            // Verificar superposición de fechas
            if (fechasSeSuperponen(fechaInicio, fechaFin, 
                                   proceso.getFechaInicio(), proceso.getFechaFin())) {
                throw new BusinessException(
                    String.format("El proceso se superpone con el proceso activo '%s' (del %s al %s)",
                        proceso.getNombre(), proceso.getFechaInicio(), proceso.getFechaFin()));
            }
        }
    }
    
    private boolean fechasSeSuperponen(LocalDate inicio1, LocalDate fin1, 
                                       LocalDate inicio2, LocalDate fin2) {
        return !fin1.isBefore(inicio2) && !fin2.isBefore(inicio1);
    }
}