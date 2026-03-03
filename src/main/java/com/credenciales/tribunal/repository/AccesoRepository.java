package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Acceso;
import com.credenciales.tribunal.model.entity.AsignacionQr;
import com.credenciales.tribunal.model.entity.Qr;
import com.credenciales.tribunal.model.enums.TipoEventoAcceso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccesoRepository extends JpaRepository<Acceso, Long> {

    List<Acceso> findByQrId(Long qrId);

    List<Acceso> findByAsignacionQrId(Long asignacionQrId);

    List<Acceso> findByTipoEvento(TipoEventoAcceso tipoEvento);

    List<Acceso> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    Optional<Acceso> findFirstByQrIdOrderByFechaHoraDesc(Long qrId);

    Optional<Acceso> findFirstByAsignacionQrIdOrderByFechaHoraDesc(Long asignacionQrId);

    List<Acceso> findAllByOrderByFechaHoraDesc();

    Page<Acceso> findAllByOrderByFechaHoraDesc(Pageable pageable);

    @Query("SELECT COUNT(a) FROM Acceso a WHERE a.tipoEvento = :tipoEvento AND a.fechaHora BETWEEN :inicio AND :fin")
    long countByTipoEventoAndFechaHoraBetween(
            @Param("tipoEvento") TipoEventoAcceso tipoEvento,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Acceso a " +
            "WHERE a.qr.id = :qrId AND a.tipoEvento = 'ENTRADA' " +
            "AND NOT EXISTS (SELECT a2 FROM Acceso a2 WHERE a2.qr.id = :qrId " +
            "AND a2.tipoEvento = 'SALIDA' AND a2.fechaHora > a.fechaHora)")
    boolean tieneAccesoActivo(@Param("qrId") Long qrId);

    @Query("SELECT a FROM Acceso a WHERE DATE(a.fechaHora) = DATE(:fecha)")
    List<Acceso> findByFecha(@Param("fecha") LocalDateTime fecha);
}