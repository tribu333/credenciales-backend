package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.acceso.AccesoDTO;
import com.credenciales.tribunal.model.entity.Acceso;
import com.credenciales.tribunal.model.enums.TipoEventoAcceso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AccesoService {

    Acceso registrarAcceso(AccesoDTO accesoDTO);

    Acceso registrarEntrada(Long qrId, Long asignacionQrId);

    Acceso registrarSalida(Long qrId, Long asignacionQrId);

    List<Acceso> obtenerTodosLosAccesos();

    Acceso obtenerAccesoPorId(Long id);

    List<Acceso> obtenerAccesosPorQr(Long qrId);

    List<Acceso> obtenerAccesosPorAsignacionQr(Long asignacionQrId);

    List<Acceso> obtenerAccesosPorTipoEvento(TipoEventoAcceso tipoEvento);

    List<Acceso> obtenerAccesosPorRangoFechas(LocalDateTime inicio, LocalDateTime fin);

    Page<Acceso> obtenerAccesosPaginados(Pageable pageable);

    EstadisticasAcceso obtenerEstadisticas(LocalDateTime inicio, LocalDateTime fin);

    boolean tieneAccesoActivo(Long qrId);

    Acceso obtenerUltimoAccesoQr(Long qrId);

    void eliminarAcceso(Long id);

    interface EstadisticasAcceso {
        long getTotalEntradas();
        long getTotalSalidas();
        long getTotalAccesos();
        LocalDateTime getFechaInicio();
        LocalDateTime getFechaFin();
    }
}