package com.credenciales.tribunal.repository;


import com.credenciales.tribunal.model.entity.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Long> {
    
    Optional<Unidad> findByNombre(String nombre);
    
    Optional<Unidad> findByAbreviatura(String abreviatura);
    
    List<Unidad> findByEstadoTrue();
}