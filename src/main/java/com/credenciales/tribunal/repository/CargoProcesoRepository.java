package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Cargo;
import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import com.credenciales.tribunal.model.entity.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargoProcesoRepository extends JpaRepository<Cargo, Long> {
    
    List<Cargo> findByProceso(ProcesoElectoral proceso);
    
    List<Cargo> findByProcesoId(Long procesoId);
    
    List<Cargo> findByUnidad(Unidad unidad);
    // Buscar por unidad
    List<Cargo> findByUnidadId(Long unidadId);
    // Buscar por nombre (exacto) en un proceso
    Optional<Cargo> findByProcesoIdAndNombre(Long procesoId, String nombre);
    Optional<Cargo> findByIdAndProcesoId(Long cargoProcesoId, Long procesoId);
    // Buscar por nombre que contenga
    List<Cargo> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar cargos activos en un proceso (considerando el campo activo si existe)
    /* @Query("SELECT cp FROM Cargo cp WHERE cp.proceso.id = :procesoId AND cp.activo = true")
    List<Cargo> findActivosByProcesoId(@Param("procesoId") Long procesoId); */
    
    // Buscar cargos por proceso con sus relaciones
    @Query("SELECT cp FROM Cargo cp " +
           "LEFT JOIN FETCH cp.unidad u " +
           "LEFT JOIN FETCH cp.proceso p " +
           "WHERE cp.proceso.id = :procesoId")
    List<Cargo> findByProcesoIdWithRelations(@Param("procesoId") Long procesoId);
    
    // Buscar cargo por ID con todas sus relaciones
    @Query("SELECT cp FROM Cargo cp " +
           "LEFT JOIN FETCH cp.unidad u " +
           "LEFT JOIN FETCH cp.proceso p " +
           "LEFT JOIN FETCH p.imagen " +
           "LEFT JOIN FETCH cp.historiales h " +
           "WHERE cp.id = :id")
    Optional<Cargo> findByIdWithAllRelations(@Param("id") Long id);
    
    // Buscar cargos por unidad en un proceso específico
    @Query("SELECT cp FROM Cargo cp " +
           "WHERE cp.proceso.id = :procesoId AND cp.unidad.id = :unidadId")
    List<Cargo> findByProcesoIdAndUnidadId(
            @Param("procesoId") Long procesoId, 
            @Param("unidadId") Long unidadId);
    
    // Contar cargos por proceso
    Long countByProcesoId(Long procesoId);
    
    // Contar cargos por unidad
    Long countByUnidadId(Long unidadId);
    
    // Buscar cargos con cantidad de historiales
    @Query("SELECT cp, SIZE(cp.historiales) as totalHistoriales FROM Cargo cp " +
           "WHERE cp.proceso.id = :procesoId")
    List<Object[]> findByProcesoIdWithHistorialCount(@Param("procesoId") Long procesoId);
    
    // Buscar cargos ordenados por nombre
    List<Cargo> findByProcesoIdOrderByNombreAsc(Long procesoId);
    
    // Buscar cargos por unidad ordenados
    List<Cargo> findByUnidadIdOrderByNombreAsc(Long unidadId);

    boolean existsByProcesoIdAndNombre(Long procesoId, String nombre);
}