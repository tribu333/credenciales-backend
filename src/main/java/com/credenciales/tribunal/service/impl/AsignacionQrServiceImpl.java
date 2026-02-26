package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.model.entity.AsignacionQr;
import com.credenciales.tribunal.model.entity.Externo;
import com.credenciales.tribunal.model.entity.Qr;
import com.credenciales.tribunal.repository.AsignacionQrRepository;
import com.credenciales.tribunal.repository.ExternoRepository;
import com.credenciales.tribunal.repository.QrRepository;
import com.credenciales.tribunal.service.AsignacionQrService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AsignacionQrServiceImpl implements AsignacionQrService {

    private final AsignacionQrRepository asignacionQrRepository;
    private final ExternoRepository externoRepository;
    private final QrRepository qrRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionQr> findAll() {
        log.info("Buscando todas las asignaciones QR");
        return asignacionQrRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public AsignacionQr findById(Long id) {
        log.info("Buscando asignación QR por ID: {}", id);
        return asignacionQrRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asignación QR no encontrada con ID: " + id));
    }

    @Override
    public AsignacionQr save(AsignacionQr asignacionQr) {
        log.info("Guardando nueva asignación QR");
        
        // Validar que el QR no esté ya asignado activamente
        if (asignacionQr.getQr() != null && asignacionQr.getQr().getId() != null) {
            validarQrDisponible(asignacionQr.getQr().getId());
        }
        
        // Validar que el externo no tenga ya una asignación activa
        if (asignacionQr.getExterno() != null && asignacionQr.getExterno().getId() != null) {
            validarExternoSinAsignacionActiva(asignacionQr.getExterno().getId());
        }
        
        // Establecer fecha de asignación si no viene
        if (asignacionQr.getFechaAsignacion() == null) {
            asignacionQr.setFechaAsignacion(LocalDateTime.now());
        }
        
        // Asegurar que activo sea true por defecto
        if (asignacionQr.getActivo() == null) {
            asignacionQr.setActivo(true);
        }
        
        return asignacionQrRepository.save(asignacionQr);
    }

    @Override
    public AsignacionQr update(Long id, AsignacionQr asignacionQr) {
        log.info("Actualizando asignación QR con ID: {}", id);
        
        AsignacionQr existingAsignacion = findById(id);
        
        // Actualizar campos permitidos
        if (asignacionQr.getExterno() != null) {
            Externo externo = externoRepository.findById(asignacionQr.getExterno().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Externo no encontrado con ID: " + asignacionQr.getExterno().getId()));
            existingAsignacion.setExterno(externo);
        }
        
        if (asignacionQr.getQr() != null) {
            Qr qr = qrRepository.findById(asignacionQr.getQr().getId())
                    .orElseThrow(() -> new EntityNotFoundException("QR no encontrado con ID: " + asignacionQr.getQr().getId()));
            existingAsignacion.setQr(qr);
        }
        
        if (asignacionQr.getFechaLiberacion() != null) {
            existingAsignacion.setFechaLiberacion(asignacionQr.getFechaLiberacion());
        }
        
        if (asignacionQr.getActivo() != null) {
            existingAsignacion.setActivo(asignacionQr.getActivo());
        }
        
        return asignacionQrRepository.save(existingAsignacion);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Eliminando asignación QR con ID: {}", id);
        
        if (!asignacionQrRepository.existsById(id)) {
            throw new EntityNotFoundException("Asignación QR no encontrada con ID: " + id);
        }
        
        asignacionQrRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionQr> findByExternoId(Long externoId) {
        log.info("Buscando asignaciones QR por ID de externo: {}", externoId);
        
        if (!externoRepository.existsById(externoId)) {
            throw new EntityNotFoundException("Externo no encontrado con ID: " + externoId);
        }
        
        return asignacionQrRepository.findByExternoId(externoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionQr> findByExternoIdAndActivoTrue(Long externoId) {
        log.info("Buscando asignaciones QR activas por ID de externo: {}", externoId);
        
        if (!externoRepository.existsById(externoId)) {
            throw new EntityNotFoundException("Externo no encontrado con ID: " + externoId);
        }
        
        return asignacionQrRepository.findByExternoIdAndActivoTrue(externoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsignacionQr> findActivaByExternoId(Long externoId) {
        log.info("Buscando asignación QR activa por ID de externo: {}", externoId);
        
        if (!externoRepository.existsById(externoId)) {
            throw new EntityNotFoundException("Externo no encontrado con ID: " + externoId);
        }
        
        return asignacionQrRepository.findActivaByExternoId(externoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionQr> findByActivoTrue() {
        log.info("Buscando todas las asignaciones QR activas");
        return asignacionQrRepository.findByActivoTrue();
    }

    @Override
    public AsignacionQr liberarAsignacion(Long id) {
        log.info("Liberando asignación QR con ID: {}", id);
        
        AsignacionQr asignacion = findById(id);
        
        if (!asignacion.getActivo()) {
            throw new IllegalStateException("La asignación ya se encuentra liberada");
        }
        
        asignacion.setActivo(false);
        asignacion.setFechaLiberacion(LocalDateTime.now());
        
        return asignacionQrRepository.save(asignacion);
    }

    @Override
    public boolean existsByExternoIdAndActivoTrue(Long externoId) {
        log.info("Verificando si externo con ID: {} tiene asignación activa", externoId);
        return asignacionQrRepository.existsByExternoIdAndActivoTrue(externoId);
    }

    /**
     * Valida que un QR esté disponible para ser asignado
     */
    private void validarQrDisponible(Long qrId) {
        Optional<AsignacionQr> asignacionActiva = asignacionQrRepository.findByQrIdAndActivoTrue(qrId);
        if (asignacionActiva.isPresent()) {
            throw new IllegalStateException("El QR con ID: " + qrId + " ya está asignado activamente");
        }
    }

    /**
     * Valida que un externo no tenga una asignación activa
     */
    private void validarExternoSinAsignacionActiva(Long externoId) {
        if (asignacionQrRepository.existsByExternoIdAndActivoTrue(externoId)) {
            throw new IllegalStateException("El externo con ID: " + externoId + " ya tiene una asignación activa");
        }
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
        Qr qr = qrRepository.findById(request.getQrId())
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