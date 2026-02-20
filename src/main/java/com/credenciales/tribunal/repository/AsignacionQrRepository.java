package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.AsignacionQr;
import com.credenciales.tribunal.model.entity.Externo;
import com.credenciales.tribunal.model.entity.Qr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionQrRepository extends JpaRepository<AsignacionQr, Long> {
    
    List<AsignacionQr> findByExterno(Externo externo);
    
    List<AsignacionQr> findByExternoId(Long externoId);
    
    List<AsignacionQr> findByExternoIdAndActivoTrue(Long externoId);

    // Buscar asignaciones activas vencidas
    List<AsignacionQr> findByActivoTrueAndFechaLiberacionBefore(LocalDateTime fecha);

    // Buscar asignación activa actual de un externo
    Optional<AsignacionQr> findFirstByExternoIdAndActivoTrueOrderByFechaAsignacionDesc(Long externoId);

    // Historial de asignaciones de un externo
    List<AsignacionQr> findByExternoIdOrderByFechaAsignacionDesc(Long externoId);
    Optional<AsignacionQr> findByQrIdAndActivoTrue(Long qrId);
    
    List<AsignacionQr> findByActivoTrue();
    
    @Query("SELECT a FROM AsignacionQr a WHERE a.externo.id = :externoId AND a.activo = true")
    Optional<AsignacionQr> findActivaByExternoId(@Param("externoId") Long externoId);

    // Verificar si un externo tiene asignación activa
    boolean existsByExternoIdAndActivoTrue(Long externoId);
}