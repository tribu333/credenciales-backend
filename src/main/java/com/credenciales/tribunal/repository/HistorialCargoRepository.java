package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.HistorialCargo;
import com.credenciales.tribunal.model.entity.Personal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialCargoRepository extends JpaRepository<HistorialCargo, Long> {
    
    List<HistorialCargo> findByPersonal(Personal personal);
    
    List<HistorialCargo> findByPersonalId(Long personalId);
    
    List<HistorialCargo> findByActivoTrue();
    
    @Query("SELECT hc FROM HistorialCargo hc WHERE hc.personal.id = :personalId AND hc.activo = true")
    List<HistorialCargo> findActivosByPersonalId(@Param("personalId") Long personalId);
    
    @Query("SELECT hc FROM HistorialCargo hc WHERE hc.fechaFin IS NULL OR hc.fechaFin > :fecha")
    List<HistorialCargo> findVigentesEnFecha(@Param("fecha") LocalDateTime fecha);
}