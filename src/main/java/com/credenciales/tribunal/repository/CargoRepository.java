package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Cargo;
import com.credenciales.tribunal.model.entity.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Long> {
    
    List<Cargo> findByUnidad(Unidad unidad);
    
    List<Cargo> findByUnidadId(Long unidadId);
    
    boolean existsByNombreAndUnidadId(String nombre, Long unidadId);
}