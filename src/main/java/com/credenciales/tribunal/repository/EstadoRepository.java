package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Estado;
import com.credenciales.tribunal.model.enums.EstadoPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {
    Optional<Estado> findByNombre(String nombre);
    
    default Optional<Estado> findByEnum(EstadoPersonal estadoPersonal) {
        return findByNombre(estadoPersonal.getNombre());
    }
}
