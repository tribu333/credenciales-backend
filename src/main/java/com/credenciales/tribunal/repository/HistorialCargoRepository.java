package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.HistorialCargo;
import com.credenciales.tribunal.model.entity.Personal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistorialCargoRepository extends JpaRepository<HistorialCargo, Long> {
    
    List<HistorialCargo> findByPersonal(Personal personal);
    
    List<HistorialCargo> findByPersonalId(Long personalId);
    
    // Buscar por cargo
    List<HistorialCargo> findByCargoId(Long cargoId);
    // Buscar historiales activos por personal
    List<HistorialCargo> findByPersonalIdAndActivoTrue(Long personalId);

    // Buscar historiales activos por cargo
    List<HistorialCargo> findByCargoIdAndActivoTrue(Long cargoId);

    // Buscar historial activo de un personal en un cargo específico
    Optional<HistorialCargo> findByPersonalIdAndCargoIdAndActivoTrue(Long personalId, Long cargoId);

    // Buscar por rango de fechas
    List<HistorialCargo> findByFechaInicioBetween(LocalDateTime start, LocalDateTime end);

    // Buscar historiales que finalizaron en un rango de fechas
    List<HistorialCargo> findByFechaFinBetween(LocalDateTime start, LocalDateTime end);

    // Buscar historiales activos con personal y cargo
    @Query("SELECT h FROM HistorialCargo h " +
           "JOIN FETCH h.personal p " +
           "JOIN FETCH h.cargo c " +
           "WHERE h.activo = true")
    List<HistorialCargo> findAllActivosWithPersonalAndCargo();

    // Buscar historial por ID con todas las relaciones
    @Query("SELECT h FROM HistorialCargo h " +
           "LEFT JOIN FETCH h.personal p " +
           "LEFT JOIN FETCH h.cargo c " +
           "LEFT JOIN FETCH c.unidad u " +
           "WHERE h.id = :id")
    Optional<HistorialCargo> findByIdWithRelations(@Param("id") Long id);
    
    // Buscar historiales por personal con relaciones
    @Query("SELECT h FROM HistorialCargo h " +
           "LEFT JOIN FETCH h.cargo c " +
           "LEFT JOIN FETCH c.unidad u " +
           "WHERE h.personal.id = :personalId")
    List<HistorialCargo> findByPersonalIdWithRelations(@Param("personalId") Long personalId);
    
    // Buscar historiales por cargo con relaciones
    @Query("SELECT h FROM HistorialCargo h " +
           "LEFT JOIN FETCH h.personal p " +
           "WHERE h.cargo.id = :cargoId")
    List<HistorialCargo> findByCargoIdWithRelations(@Param("cargoId") Long cargoId);
    
    // Contar historiales activos por personal
    Long countByPersonalIdAndActivoTrue(Long personalId);
    
    // Contar historiales activos por cargo
    Long countByCargoIdAndActivoTrue(Long cargoId);
    
    // Verificar si existe un historial activo para un personal en un cargo
    boolean existsByPersonalIdAndCargoIdAndActivoTrue(Long personalId, Long cargoId);
    
    // Buscar el historial más reciente de un personal
    @Query("SELECT h FROM HistorialCargo h " +
           "WHERE h.personal.id = :personalId " +
           "ORDER BY h.fechaInicio DESC")
    List<HistorialCargo> findLatestByPersonalId(@Param("personalId") Long personalId, Pageable pageable);
    
    List<HistorialCargo> findByActivoTrue();
    
    @Query("SELECT hc FROM HistorialCargo hc WHERE hc.personal.id = :personalId AND hc.activo = true")
    List<HistorialCargo> findActivosByPersonalId(@Param("personalId") Long personalId);
    
    @Query("SELECT hc FROM HistorialCargo hc WHERE hc.fechaFin IS NULL OR hc.fechaFin > :fecha")
    List<HistorialCargo> findVigentesEnFecha(@Param("fecha") LocalDateTime fecha);
}