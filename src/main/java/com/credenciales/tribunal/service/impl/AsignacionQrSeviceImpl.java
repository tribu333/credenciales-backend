package com.credenciales.tribunal.service.impl;

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
