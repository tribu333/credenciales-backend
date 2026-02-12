package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.HistorialCargoProceso;
import com.credenciales.tribunal.model.entity.Personal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialCargoProcesoRepository extends JpaRepository<HistorialCargoProceso, Long> {
    
    List<HistorialCargoProceso> findByPersonal(Personal personal);
    
    List<HistorialCargoProceso> findByPersonalId(Long personalId);
    
    List<HistorialCargoProceso> findByCargoProcesoId(Long cargoProcesoId);
    
    List<HistorialCargoProceso> findByActivoTrue();
    
    @Query("SELECT hcp FROM HistorialCargoProceso hcp WHERE hcp.cargoProceso.id = :cargoProcesoId AND hcp.activo = true")
    List<HistorialCargoProceso> findActivosByCargoProcesoId(@Param("cargoProcesoId") Long cargoProcesoId);
}
