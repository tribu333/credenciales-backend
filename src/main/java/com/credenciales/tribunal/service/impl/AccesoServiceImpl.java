package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.acceso.AccesoDTO;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.model.entity.Acceso;
import com.credenciales.tribunal.model.entity.AsignacionQr;
import com.credenciales.tribunal.model.entity.Qr;
import com.credenciales.tribunal.model.enums.TipoEventoAcceso;
import com.credenciales.tribunal.repository.AccesoRepository;
import com.credenciales.tribunal.repository.AsignacionQrRepository;
import com.credenciales.tribunal.repository.QrRepository;
import com.credenciales.tribunal.service.AccesoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccesoServiceImpl implements AccesoService {

    private final AccesoRepository accesoRepository;
    private final QrRepository qrRepository;
    private final AsignacionQrRepository asignacionQrRepository;

    @Override
    public Acceso registrarAcceso(AccesoDTO accesoDTO) {
        log.info("Registrando nuevo acceso: {}", accesoDTO);

        validarAcceso(accesoDTO);

        Acceso acceso = Acceso.builder()
                .fechaHora(accesoDTO.getFechaHora() != null ? accesoDTO.getFechaHora() : LocalDateTime.now())
                .tipoEvento(accesoDTO.getTipoEvento())
                .build();

        // Asignar QR si existe
        if (accesoDTO.getQrId() != null) {
            Qr qr = qrRepository.findById(accesoDTO.getQrId())
                    .orElseThrow(() -> new ResourceNotFoundException("QR no encontrado con ID: " + accesoDTO.getQrId()));
            acceso.setQr(qr);
        }

        // Asignar AsignacionQR si existe
        if (accesoDTO.getAsignacionQrId() != null) {
            AsignacionQr asignacionQr = asignacionQrRepository.findById(accesoDTO.getAsignacionQrId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Asignación QR no encontrada con ID: " + accesoDTO.getAsignacionQrId()));
            acceso.setAsignacionQr(asignacionQr);
        }

        Acceso accesoGuardado = accesoRepository.save(acceso);
        log.info("Acceso registrado exitosamente con ID: {}", accesoGuardado.getId());

        return accesoGuardado;
    }

    @Override
    public Acceso registrarEntrada(Long qrId, Long asignacionQrId) {
        log.info("Registrando entrada - QR ID: {}, Asignacion ID: {}", qrId, asignacionQrId);

        if (qrId != null && tieneAccesoActivo(qrId)) {
            throw new BusinessException("El QR ya tiene un acceso activo (entrada sin salida)");
        }

        AccesoDTO accesoDTO = AccesoDTO.builder()
                .tipoEvento(TipoEventoAcceso.ENTRADA)
                .qrId(qrId)
                .asignacionQrId(asignacionQrId)
                .build();

        return registrarAcceso(accesoDTO);
    }

    @Override
    public Acceso registrarSalida(Long qrId, Long asignacionQrId) {
        log.info("Registrando salida - QR ID: {}, Asignacion ID: {}", qrId, asignacionQrId);

        if (qrId != null && !tieneAccesoActivo(qrId)) {
            throw new BusinessException("No hay una entrada activa para registrar la salida");
        }

        AccesoDTO accesoDTO = AccesoDTO.builder()
                .tipoEvento(TipoEventoAcceso.SALIDA)
                .qrId(qrId)
                .asignacionQrId(asignacionQrId)
                .build();

        return registrarAcceso(accesoDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Acceso> obtenerTodosLosAccesos() {
        log.debug("Obteniendo todos los accesos");
        return accesoRepository.findAllByOrderByFechaHoraDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Acceso obtenerAccesoPorId(Long id) {
        log.debug("Buscando acceso con ID: {}", id);
        return accesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acceso no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Acceso> obtenerAccesosPorQr(Long qrId) {
        log.debug("Buscando accesos para QR ID: {}", qrId);
        if (!qrRepository.existsById(qrId)) {
            throw new ResourceNotFoundException("QR no encontrado con ID: " + qrId);
        }
        return accesoRepository.findByQrId(qrId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Acceso> obtenerAccesosPorAsignacionQr(Long asignacionQrId) {
        log.debug("Buscando accesos para Asignacion QR ID: {}", asignacionQrId);
        if (!asignacionQrRepository.existsById(asignacionQrId)) {
            throw new ResourceNotFoundException("Asignación QR no encontrada con ID: " + asignacionQrId);
        }
        return accesoRepository.findByAsignacionQrId(asignacionQrId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Acceso> obtenerAccesosPorTipoEvento(TipoEventoAcceso tipoEvento) {
        log.debug("Buscando accesos por tipo de evento: {}", tipoEvento);
        return accesoRepository.findByTipoEvento(tipoEvento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Acceso> obtenerAccesosPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Buscando accesos entre {} y {}", inicio, fin);
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        return accesoRepository.findByFechaHoraBetween(inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Acceso> obtenerAccesosPaginados(Pageable pageable) {
        log.debug("Obteniendo accesos paginados: {}", pageable);
        return accesoRepository.findAllByOrderByFechaHoraDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasAcceso obtenerEstadisticas(LocalDateTime inicio, LocalDateTime fin) {
        log.info("Generando estadísticas de accesos entre {} y {}", inicio, fin);

        long totalEntradas = accesoRepository.countByTipoEventoAndFechaHoraBetween(
                TipoEventoAcceso.ENTRADA, inicio, fin);
        long totalSalidas = accesoRepository.countByTipoEventoAndFechaHoraBetween(
                TipoEventoAcceso.SALIDA, inicio, fin);

        final LocalDateTime fechaInicio = inicio;
        final LocalDateTime fechaFin = fin;

        return new EstadisticasAcceso() {
            @Override
            public long getTotalEntradas() {
                return totalEntradas;
            }

            @Override
            public long getTotalSalidas() {
                return totalSalidas;
            }

            @Override
            public long getTotalAccesos() {
                return totalEntradas + totalSalidas;
            }

            @Override
            public LocalDateTime getFechaInicio() {
                return fechaInicio;
            }

            @Override
            public LocalDateTime getFechaFin() {
                return fechaFin;
            }
        };
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneAccesoActivo(Long qrId) {
        log.debug("Verificando acceso activo para QR ID: {}", qrId);
        return accesoRepository.tieneAccesoActivo(qrId);
    }

    @Override
    @Transactional(readOnly = true)
    public Acceso obtenerUltimoAccesoQr(Long qrId) {
        log.debug("Obteniendo último acceso para QR ID: {}", qrId);
        return accesoRepository.findFirstByQrIdOrderByFechaHoraDesc(qrId)
                .orElse(null);
    }

    @Override
    public void eliminarAcceso(Long id) {
        log.warn("Eliminando acceso con ID: {}", id);
        if (!accesoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Acceso no encontrado con ID: " + id);
        }
        accesoRepository.deleteById(id);
        log.info("Acceso eliminado exitosamente con ID: {}", id);
    }

    private void validarAcceso(AccesoDTO accesoDTO) {
        if (accesoDTO.getTipoEvento() == null) {
            throw new BusinessException("El tipo de evento es obligatorio");
        }

        if (accesoDTO.getQrId() == null && accesoDTO.getAsignacionQrId() == null) {
            throw new BusinessException("Debe proporcionar al menos un QR ID o una Asignación QR ID");
        }

        if (accesoDTO.getQrId() != null && !qrRepository.existsById(accesoDTO.getQrId())) {
            throw new ResourceNotFoundException("QR no encontrado con ID: " + accesoDTO.getQrId());
        }

        if (accesoDTO.getAsignacionQrId() != null && !asignacionQrRepository.existsById(accesoDTO.getAsignacionQrId())) {
            throw new ResourceNotFoundException("Asignación QR no encontrada con ID: " + accesoDTO.getAsignacionQrId());
        }
    }
}