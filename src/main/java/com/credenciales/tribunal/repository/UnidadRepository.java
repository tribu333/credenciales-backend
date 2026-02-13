package com.credenciales.tribunal.repository;


import com.credenciales.tribunal.model.entity.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Long> {
    
    Optional<Unidad> findByNombre(String nombre);
    
    Optional<Unidad> findByAbreviatura(String abreviatura);
    
    List<Unidad> findByEstadoTrue();
    // Buscar por estado
    List<Unidad> findByEstado(Boolean estado);
    
    // Buscar por nombre que contenga (like)
    List<Unidad> findByNombreContainingIgnoreCase(String nombre);
    
    // Verificar si existe por nombre
    boolean existsByNombre(String nombre);
    
    // Verificar si existe por abreviatura
    boolean existsByAbreviatura(String abreviatura);
    
    // Buscar unidades con sus cargos
    @Query("SELECT u FROM Unidad u LEFT JOIN FETCH u.cargos WHERE u.id = :id")
    Optional<Unidad> findByIdWithCargos(@Param("id") Long id);
    
    // Buscar unidades con sus cargos en proceso
    @Query("SELECT u FROM Unidad u LEFT JOIN FETCH u.cargosProceso WHERE u.id = :id")
    Optional<Unidad> findByIdWithCargosProceso(@Param("id") Long id);
    
    // Buscar todas las unidades activas ordenadas por nombre
    List<Unidad> findByEstadoTrueOrderByNombreAsc();
}