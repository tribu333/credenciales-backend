package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer> {
    
    Optional<Estado> findByNombreEstado(String nombreEstado);
    
    List<Estado> findByValorEstado(Boolean valorEstado);
    
    boolean existsByNombreEstado(String nombreEstado);
}