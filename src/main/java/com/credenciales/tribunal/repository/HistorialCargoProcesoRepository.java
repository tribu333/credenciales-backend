package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.HistorialCargoProceso;
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
public interface HistorialCargoProcesoRepository extends JpaRepository<HistorialCargoProceso, Long> {
    
    List<HistorialCargoProceso> findByPersonal(Personal personal);
    
    List<HistorialCargoProceso> findByPersonalId(Long personalId);
    
    List<HistorialCargoProceso> findByCargoProcesoId(Long cargoProcesoId);
    
    List<HistorialCargoProceso> findByActivoTrue();
    
    @Query("SELECT hcp FROM HistorialCargoProceso hcp WHERE hcp.cargoProceso.id = :cargoProcesoId AND hcp.activo = true")
    List<HistorialCargoProceso> findActivosByCargoProcesoId(@Param("cargoProcesoId") Long cargoProcesoId);
    // Buscar historiales activos por cargo proceso
    List<HistorialCargoProceso> findByCargoProcesoIdAndActivoTrue(Long cargoProcesoId);

    // Buscar historiales activos por personal
    List<HistorialCargoProceso> findByPersonalIdAndActivoTrue(Long personalId);
    
    // Buscar historial activo de un personal en un cargo proceso específico
    Optional<HistorialCargoProceso> findByPersonalIdAndCargoProcesoIdAndActivoTrue(
            Long personalId, Long cargoProcesoId);
    
    // Buscar por rango de fechas
    List<HistorialCargoProceso> findByFechaInicioBetween(LocalDateTime start, LocalDateTime end);
    
    // Buscar historiales que finalizaron en un rango de fechas
    List<HistorialCargoProceso> findByFechaFinBetween(LocalDateTime start, LocalDateTime end);
    
    // Buscar historiales activos con todas las relaciones
    @Query("SELECT h FROM HistorialCargoProceso h " +
           "JOIN FETCH h.personal p " +
           "JOIN FETCH h.cargoProceso cp " +
           "JOIN FETCH cp.proceso " +
           "JOIN FETCH cp.unidad " +
           "WHERE h.activo = true")
    List<HistorialCargoProceso> findAllActivosWithRelations();
    
    // Buscar historial por ID con todas las relaciones
    @Query("SELECT h FROM HistorialCargoProceso h " +
           "LEFT JOIN FETCH h.personal p " +
           "LEFT JOIN FETCH h.cargoProceso cp " +
           "LEFT JOIN FETCH cp.proceso pr " +
           "LEFT JOIN FETCH cp.unidad u " +
           "WHERE h.id = :id")
    Optional<HistorialCargoProceso> findByIdWithAllRelations(@Param("id") Long id);
    
    // Buscar historiales por cargo proceso con relaciones
    @Query("SELECT h FROM HistorialCargoProceso h " +
           "LEFT JOIN FETCH h.personal p " +
           "WHERE h.cargoProceso.id = :cargoProcesoId")
    List<HistorialCargoProceso> findByCargoProcesoIdWithPersonal(@Param("cargoProcesoId") Long cargoProcesoId);
    
    // Buscar historiales por personal con relaciones
    @Query("SELECT h FROM HistorialCargoProceso h " +
           "LEFT JOIN FETCH h.cargoProceso cp " +
           "LEFT JOIN FETCH cp.proceso " +
           "LEFT JOIN FETCH cp.unidad " +
           "WHERE h.personal.id = :personalId")
    List<HistorialCargoProceso> findByPersonalIdWithCargoProceso(@Param("personalId") Long personalId);
    
    // Buscar historiales por proceso
    @Query("SELECT h FROM HistorialCargoProceso h " +
           "WHERE h.cargoProceso.proceso.id = :procesoId")
    List<HistorialCargoProceso> findByProcesoId(@Param("procesoId") Long procesoId);
    
    // Buscar historiales activos por proceso
    @Query("SELECT h FROM HistorialCargoProceso h " +
           "WHERE h.cargoProceso.proceso.id = :procesoId AND h.activo = true")
    List<HistorialCargoProceso> findActivosByProcesoId(@Param("procesoId") Long procesoId);
    
    // Buscar historiales por unidad
    @Query("SELECT h FROM HistorialCargoProceso h " +
           "WHERE h.cargoProceso.unidad.id = :unidadId")
    List<HistorialCargoProceso> findByUnidadId(@Param("unidadId") Long unidadId);
    
    // Contar historiales activos por cargo proceso
    Long countByCargoProcesoIdAndActivoTrue(Long cargoProcesoId);
    
    // Contar historiales activos por personal
    Long countByPersonalIdAndActivoTrue(Long personalId);
    
    // Contar historiales activos por proceso
    @Query("SELECT COUNT(h) FROM HistorialCargoProceso h " +
           "WHERE h.cargoProceso.proceso.id = :procesoId AND h.activo = true")
    Long countActivosByProcesoId(@Param("procesoId") Long procesoId);
    
    // Verificar si existe un historial activo
    boolean existsByPersonalIdAndCargoProcesoIdAndActivoTrue(Long personalId, Long cargoProcesoId);
    
    // Buscar el historial más reciente de un personal
    @Query("SELECT h FROM HistorialCargoProceso h " +
           "WHERE h.personal.id = :personalId " +
           "ORDER BY h.fechaInicio DESC")
    List<HistorialCargoProceso> findLatestByPersonalId(@Param("personalId") Long personalId, Pageable pageable);
    
    // Buscar historiales por estado
    List<HistorialCargoProceso> findByActivo(Boolean activo);
    
    // Buscar historiales que no tienen fecha de fin (aún activos)
    List<HistorialCargoProceso> findByFechaFinIsNullAndActivoTrue();

    @Query("SELECT h FROM HistorialCargoProceso h " +
       "LEFT JOIN FETCH h.cargoProceso cp " +
       "LEFT JOIN FETCH cp.unidad u " +
       "WHERE h.personal.id IN :personalIds AND h.activo = true")
    List<HistorialCargoProceso> findByPersonalIdInAndActivoTrue(@Param("personalIds") List<Long> personalIds);
}
