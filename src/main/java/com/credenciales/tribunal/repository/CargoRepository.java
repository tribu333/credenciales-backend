package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Cargo;
import com.credenciales.tribunal.model.entity.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Long> {

     // Buscar por nombre (exacto)
    Optional<Cargo> findByNombre(String nombre);
    
    // Buscar por nombre que contenga (like)
    List<Cargo> findByNombreContainingIgnoreCase(String nombre);

    // Buscar por unidad con detalles
    @Query("SELECT c FROM Cargo c LEFT JOIN FETCH c.unidad WHERE c.unidad.id = :unidadId")
    List<Cargo> findByUnidadIdWithUnidad(@Param("unidadId") Long unidadId);
    
    // Verificar si existe por nombre
    boolean existsByNombre(String nombre);

    boolean existsByNombreAndUnidadId(String nombre, Long unidadId);
     // Buscar cargo con su historial
    @Query("SELECT c FROM Cargo c LEFT JOIN FETCH c.historiales WHERE c.id = :id")
    Optional<Cargo> findByIdWithHistorial(@Param("id") Long id);

    // Buscar cargos con sus historiales por unidad
    @Query("SELECT c FROM Cargo c LEFT JOIN FETCH c.historiales WHERE c.unidad.id = :unidadId")
    List<Cargo> findByUnidadIdWithHistorial(@Param("unidadId") Long unidadId);

    List<Cargo> findByUnidad(Unidad unidad);
    
    List<Cargo> findByUnidadId(Long unidadId);

    // Contar cargos por unidad
    Long countByUnidadId(Long unidadId);
    
    // Buscar cargos ordenados
    List<Cargo> findAllByOrderByNombreAsc();
    
    // Buscar cargos por unidad ordenados
    List<Cargo> findByUnidadIdOrderByNombreAsc(Long unidadId);
}