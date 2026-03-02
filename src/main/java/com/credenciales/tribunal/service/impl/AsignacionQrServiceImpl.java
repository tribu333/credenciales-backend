package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.asignacionesqr.AsignacionRequestDTO;
import com.credenciales.tribunal.dto.asignacionesqr.AsignacionResponseDTO;
import com.credenciales.tribunal.model.entity.AsignacionQr;
import com.credenciales.tribunal.model.entity.Externo;
import com.credenciales.tribunal.repository.AsignacionQrRepository;
import com.credenciales.tribunal.repository.ExternoRepository;
import com.credenciales.tribunal.service.AsignacionQrService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AsignacionQrServiceImpl implements AsignacionQrService {

    private final AsignacionQrRepository asignacionQrRepository;
    private final ExternoRepository externoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionResponseDTO> findAll() {
        log.info("Buscando todas las asignaciones QR");
        return asignacionQrRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AsignacionResponseDTO findById(Long id) {
        log.info("Buscando asignación QR por ID: {}", id);
        AsignacionQr asignacion = asignacionQrRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asignación QR no encontrada con ID: " + id));
        return mapToResponseDTO(asignacion);
    }

    @Override
    public AsignacionResponseDTO create(AsignacionRequestDTO requestDTO) {
        log.info("Creando nueva asignación QR para externoId: {} y qrId: {}", 
                requestDTO.getExternoId(), requestDTO.getQrCod());
        
        // 1. Validar disponibilidad y liberar asignaciones previas si es necesario
        liberarAsignacionesPreviasSiExisten(requestDTO.getQrCod(), requestDTO.getExternoId());
        
        // 2. Validar que el QR esté disponible AHORA (después de liberar)
        
        
        // 3. Validar que el externo esté disponible AHORA (después de liberar)
        validarExternoSinAsignacionActiva(requestDTO.getExternoId());
        
        // 4. Obtener entidades
        Externo externo = externoRepository.findById(requestDTO.getExternoId())
                .orElseThrow(() -> new EntityNotFoundException("Externo no encontrado con ID: " + requestDTO.getExternoId()));
        
        
        // 6. Crear asignación
        AsignacionQr asignacion = AsignacionQr.builder()
                .externo(externo)
                .qr(requestDTO.getQrCod())
                .fechaAsignacion(LocalDateTime.now())
                .activo(true)
                .build();
        
        AsignacionQr savedAsignacion = asignacionQrRepository.save(asignacion);
        
        
        
        return mapToResponseDTO(savedAsignacion);
    }

    @Override
    public AsignacionResponseDTO update(Long id, AsignacionRequestDTO requestDTO) {
        log.info("Actualizando asignación QR con ID: {}", id);
        
        AsignacionQr existingAsignacion = asignacionQrRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asignación QR no encontrada con ID: " + id));
        
        // Si se está cambiando el externo
        if (requestDTO.getExternoId() != null && 
            !requestDTO.getExternoId().equals(existingAsignacion.getExterno().getId())) {
            
            validarExternoSinAsignacionActiva(requestDTO.getExternoId());
            
            Externo externo = externoRepository.findById(requestDTO.getExternoId())
                    .orElseThrow(() -> new EntityNotFoundException("Externo no encontrado con ID: " + requestDTO.getExternoId()));
            existingAsignacion.setExterno(externo);
        }
        
        // Si se está cambiando el QR
        existingAsignacion.setQr(requestDTO.getQrCod());
        
        // Actualizar fecha de liberación si se proporciona
        if (requestDTO.getFechaLiberacion() != null) {
            existingAsignacion.setFechaLiberacion(requestDTO.getFechaLiberacion());
        }
        
        AsignacionQr updatedAsignacion = asignacionQrRepository.save(existingAsignacion);
        log.info("Asignación QR actualizada con ID: {}", updatedAsignacion.getId());
        
        return mapToResponseDTO(updatedAsignacion);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Eliminando (soft delete) asignación QR con ID: {}", id);
        
        AsignacionQr asignacion = asignacionQrRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asignación QR no encontrada con ID: " + id));
        
        // Soft delete (opcional - puedes simplemente no hacer nada si quieres mantener historial)
        // asignacionRepository.delete(asignacion); // NO hacer esto, mejor:
        
        // Si quieres marcarla como inactiva:
        asignacion.setActivo(false);
        asignacion.setFechaLiberacion(LocalDateTime.now());
        asignacionQrRepository.save(asignacion);
        
        log.info("Asignación QR marcada como inactiva con ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionResponseDTO> findByExternoId(Long externoId) {
        log.info("Buscando asignaciones QR por ID de externo: {}", externoId);
        
        if (!externoRepository.existsById(externoId)) {
            throw new EntityNotFoundException("Externo no encontrado con ID: " + externoId);
        }
        
        return asignacionQrRepository.findByExternoId(externoId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionResponseDTO> findByExternoIdAndActivoTrue(Long externoId) {
        log.info("Buscando asignaciones QR activas por ID de externo: {}", externoId);
        
        if (!externoRepository.existsById(externoId)) {
            throw new EntityNotFoundException("Externo no encontrado con ID: " + externoId);
        }
        
        return asignacionQrRepository.findByExternoIdAndActivoTrue(externoId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AsignacionResponseDTO findActivaByExternoId(Long externoId) {
        log.info("Buscando asignación QR activa por ID de externo: {}", externoId);
        
        if (!externoRepository.existsById(externoId)) {
            throw new EntityNotFoundException("Externo no encontrado con ID: " + externoId);
        }
        
        AsignacionQr asignacion = asignacionQrRepository.findActivaByExternoId(externoId)
                .orElseThrow(() -> new EntityNotFoundException(
                    "No se encontró una asignación activa para el externo con ID: " + externoId));
        
        return mapToResponseDTO(asignacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionResponseDTO> findByActivoTrue() {
        log.info("Buscando todas las asignaciones QR activas");
        return asignacionQrRepository.findByActivoTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AsignacionResponseDTO liberarAsignacion(Long id) {
        log.info("Liberando asignación QR con ID: {}", id);
        
        AsignacionQr asignacion = asignacionQrRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asignación QR no encontrada con ID: " + id));
        
        if (!asignacion.getActivo()) {
            throw new IllegalStateException("La asignación ya se encuentra liberada");
        }
        
        // Liberar la asignación
        asignacion.setActivo(false);
        asignacion.setFechaLiberacion(LocalDateTime.now());
        
        
        AsignacionQr liberatedAsignacion = asignacionQrRepository.save(asignacion);
        log.info("Asignación QR liberada con ID: {} ", 
                id);
        
        return mapToResponseDTO(liberatedAsignacion);
    }

    @Override
    public boolean existsByExternoIdAndActivoTrue(Long externoId) {
        log.info("Verificando si externo con ID: {} tiene asignación activa", externoId);
        return asignacionQrRepository.existsByExternoIdAndActivoTrue(externoId);
    }

    /**
     * Método para mapear entidad a DTO de respuesta
     */
    private AsignacionResponseDTO mapToResponseDTO(AsignacionQr asignacion) {
        return AsignacionResponseDTO.builder()
                .id(asignacion.getId())
                .externoNombre(asignacion.getExterno() != null ? 
                        asignacion.getExterno().getNombreCompleto(): null)
                .qrCodigo(asignacion.getQr() != null ? 
                        asignacion.getQr() : null)
                .fechaAsignacion(asignacion.getFechaAsignacion())
                .fechaLiberacion(asignacion.getFechaLiberacion())
                .activo(asignacion.getActivo())
                .build();
    }

    /**
     * Valida que un QR esté disponible para ser asignado
     */
/*     private void validarQrDisponible(Long qrId) {
        Optional<AsignacionQr> asignacionActiva = asignacionQrRepository.findByQrIdAndActivoTrue(qrId);
        
        if (asignacionActiva.isPresent()) {
            AsignacionQr asignacion = asignacionActiva.get();
            throw new IllegalStateException(
                String.format("El QR con ID: %d ya está asignado activamente al externo: %s (desde: %s)", 
                    qrId, 
                    asignacion.getExterno().getNombreCompleto(),
                    asignacion.getFechaAsignacion().toString()
                )
            );
        }
        
        // Verificar también que el QR no esté en estado INACTIVO
        Qr qr = qrRepository.findById(qrId)
            .orElseThrow(() -> new EntityNotFoundException("QR no encontrado con ID: " + qrId));
            
        if (qr.getEstado() == EstadoQr.INACTIVO) {
            throw new IllegalStateException("El QR con ID: " + qrId + " está INACTIVO y no puede asignarse");
        }
    } */

    /**
     * Valida que un externo no tenga una asignación activa
     */
    private void validarExternoSinAsignacionActiva(Long externoId) {
        Optional<AsignacionQr> asignacionActiva = asignacionQrRepository.findActivaByExternoId(externoId);
        
        if (asignacionActiva.isPresent()) {
            AsignacionQr asignacion = asignacionActiva.get();
            throw new IllegalStateException(
                String.format("El externo con ID: %d ya tiene una asignación activa del QR: %s (desde: %s)", 
                    externoId, 
                    asignacion.getQr(),
                    asignacion.getFechaAsignacion().toString()
                )
            );
        }
    }
    /**
     * NUEVO MÉTODO: Libera asignaciones previas del QR y externo si existen
     */
    private void liberarAsignacionesPreviasSiExisten(String qrId, Long externoId) {
        
        // Buscar si el externo tiene asignación activa
        Optional<AsignacionQr> asignacionExternoActiva = 
            asignacionQrRepository.findActivaByExternoId(externoId);
        
        asignacionExternoActiva.ifPresent(asignacion -> {
            log.info("Liberando asignación previa del Externo ID: {}", externoId);
            asignacion.setActivo(false);
            asignacion.setFechaLiberacion(LocalDateTime.now());
        });
        
        // Guardar todos los cambios
        asignacionQrRepository.flush();
    }
}
/* package com.credenciales.tribunal.service.impl;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;

import com.credenciales.tribunal.dto.asignacionesqr.AsignacionRequestDTO;
import com.credenciales.tribunal.dto.asignacionesqr.AsignacionResponseDTO;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.model.entity.AsignacionQr;
import com.credenciales.tribunal.model.entity.Externo;
import com.credenciales.tribunal.model.entity.Qr;
import com.credenciales.tribunal.repository.AsignacionQrRepository;
import com.credenciales.tribunal.repository.ExternoRepository;
import com.credenciales.tribunal.repository.QrRepository;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
@Transactional
public class AsignacionQrSeviceImpl {

    AsignacionQrRepository asignacionQrRepository;
    ExternoRepository externoRepository;
    QrRepository qrRepository;
     public AsignacionResponseDTO asignarQR(AsignacionRequestDTO request) {
        // 1. Validar que el externo existe
        Externo externo = externoRepository.findById(request.getExternoId())
            .orElseThrow(() -> new ResourceNotFoundException("Externo no encontrado"));
        
        // 2. Validar que el QR existe y está disponible
        Qr qr = qrRepository.findById(request.getQrCod())
            .orElseThrow(() -> new ResourceNotFoundException("QR no encontrado"));
        
        if (qr.isAsignado()) {
            throw new BusinessException("El QR ya está asignado a otra persona");
        }
        
        // 3. Desactivar asignaciones previas del externo
        desactivarAsignacionesPrevias(externo.getId());
        
        // 4. Crear nueva asignación
        AsignacionQr nuevaAsignacion = AsignacionQr.builder()
            .externo(externo)
            .qr(qr)
            .fechaAsignacion(LocalDateTime.now())
            .fechaLiberacion(calcularFechaLiberacion(request))
            .activo(true)
            .build();
        
        AsignacionQr saved = asignacionQrRepository.save(nuevaAsignacion);
        
        // 5. Marcar QR como asignado
        qr.setAsignado(true);
        qrRepository.save(qr);
        
        log.info("QR asignado exitosamente. Externo: {}, QR: {}", 
            externo.getId(), qr.getId());
        
        return mapper.toResponseDTO(saved);
    }
    
    public void liberarAsignacion(Long asignacionId, MotivoLiberacion motivo) {
        AsignacionQr asignacion = asignacionQrRepository.findById(asignacionId)
            .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada"));
        
        if (!asignacion.getActivo()) {
            throw new BusinessException("La asignación ya está liberada");
        }
        
        // Liberar la asignación
        asignacion.setActivo(false);
        asignacion.setFechaLiberacion(LocalDateTime.now());
        
        // Liberar el QR
        asignacion.getQr().setAsignado(false);
        
        log.info("Asignación liberada. ID: {}, Motivo: {}", asignacionId, motivo);
    }
    
    @Scheduled(cron = "0 0 0 * * *") // Ejecutar diariamente
    @Transactional
    public void liberarAsignacionesVencidas() {
        log.info("Iniciando liberación automática de asignaciones vencidas");
        
        List<AsignacionQr> vencidas = asignacionQrRepository
            .findByActivoTrueAndFechaLiberacionBefore(LocalDateTime.now());
        
        vencidas.forEach(asignacion -> {
            asignacion.setActivo(false);
            asignacion.getQr().setAsignado(false);
            log.info("Asignación liberada automáticamente: {}", asignacion.getId());
        });
        
        log.info("Liberación automática completada. Total: {}", vencidas.size());
    }
    
    private void desactivarAsignacionesPrevias(Long externoId) {
        List<AsignacionQr> activas = asignacionQrRepository
            .findByExternoIdAndActivoTrue(externoId);
        
        if (!activas.isEmpty()) {
            log.info("Desactivando {} asignaciones previas del externo {}", 
                activas.size(), externoId);
            
            activas.forEach(asignacion -> {
                asignacion.setActivo(false);
                asignacion.setFechaLiberacion(LocalDateTime.now());
                asignacion.getQr().setAsignado(false);
            });
        }
    }
    
    private LocalDateTime calcularFechaLiberacion(AsignacionRequestDTO request) {
        // Lógica para calcular fecha de liberación
        // Por ejemplo: 30 días después, o según configuración
        return request.getFechaLiberacion() != null ? 
            request.getFechaLiberacion() : 
            LocalDateTime.now().plusDays(30);
    }
}
 */