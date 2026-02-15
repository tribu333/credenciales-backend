package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcesoElectoralRepository extends JpaRepository<ProcesoElectoral, Long> {
    
    List<ProcesoElectoral> findByEstadoTrue();

    // Buscar por nombre (exacto)
    Optional<ProcesoElectoral> findByNombre(String nombre);
    
    List<ProcesoElectoral> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar por estado
    List<ProcesoElectoral> findByEstado(Boolean estado);

    // Buscar procesos activos ordenados por fecha de inicio
    List<ProcesoElectoral> findByEstadoTrueOrderByFechaInicioDesc();

    // Buscar procesos por rango de fechas
    List<ProcesoElectoral> findByFechaInicioBetween(LocalDate start, LocalDate end);

    // Buscar procesos que estén vigentes en una fecha específica
    @Query("SELECT p FROM ProcesoElectoral p WHERE :fecha BETWEEN p.fechaInicio AND p.fechaFin")
    List<ProcesoElectoral> findVigentesEnFecha(@Param("fecha") LocalDate fecha);

    // Buscar procesos activos vigentes
    @Query("SELECT p FROM ProcesoElectoral p WHERE p.estado = true AND :fecha BETWEEN p.fechaInicio AND p.fechaFin")
    List<ProcesoElectoral> findActivosVigentesEnFecha(@Param("fecha") LocalDate fecha);

    // Buscar procesos próximos a iniciar
    @Query("SELECT p FROM ProcesoElectoral p WHERE p.fechaInicio > :fecha ORDER BY p.fechaInicio ASC")
    List<ProcesoElectoral> findProximosAIniciar(@Param("fecha") LocalDate fecha);
    
    // Buscar procesos finalizados
    @Query("SELECT p FROM ProcesoElectoral p WHERE p.fechaFin < :fecha ORDER BY p.fechaFin DESC")
    List<ProcesoElectoral> findFinalizados(@Param("fecha") LocalDate fecha);
    
    @Query("SELECT p FROM ProcesoElectoral p WHERE p.fechaInicio <= :fecha AND p.fechaFin >= :fecha AND p.estado = true")
    List<ProcesoElectoral> findActivosEnFecha(@Param("fecha") LocalDate fecha);
    
    @Query("SELECT p FROM ProcesoElectoral p WHERE p.fechaFin < :fecha")
    List<ProcesoElectoral> findFinalizadosAntesDe(@Param("fecha") LocalDate fecha);

    // Buscar proceso con su imagen
    @Query("SELECT p FROM ProcesoElectoral p LEFT JOIN FETCH p.imagen WHERE p.id = :id")
    Optional<ProcesoElectoral> findByIdWithImagen(@Param("id") Long id);

    // Buscar proceso con sus cargos
/*     @Query("SELECT p FROM ProcesoElectoral p LEFT JOIN FETCH p.cargosProceso c WHERE p.id = :id")
    Optional<ProcesoElectoral> findByIdWithCargos(@Param("id") Long id); */
    
    // Buscar proceso con imagen y cargos
    /* @Query("SELECT p FROM ProcesoElectoral p " +
           "LEFT JOIN FETCH p.imagen " +
           "LEFT JOIN FETCH p.cargosProceso c " +
           "LEFT JOIN FETCH c.cargo " +
           "LEFT JOIN FETCH c.cargo.unidad " +
           "WHERE p.id = :id")
    Optional<ProcesoElectoral> findByIdWithAllRelations(@Param("id") Long id); */
    
    // Buscar procesos con cantidad de cargos
    @Query("SELECT p, SIZE(p.cargosProceso) as totalCargos FROM ProcesoElectoral p")
    List<Object[]> findAllWithCargosCount();
    
    // Verificar si existe por nombre
    boolean existsByNombre(String nombre);
    
    // Contar procesos por estado
    Long countByEstado(Boolean estado);
    
    // Buscar procesos por estado y rango de fechas
    List<ProcesoElectoral> findByEstadoAndFechaInicioBetween(Boolean estado, LocalDate start, LocalDate end);
    
    // Buscar el proceso más reciente
    Optional<ProcesoElectoral> findTopByOrderByFechaInicioDesc();
}