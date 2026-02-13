package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.EstadoActual;
import com.credenciales.tribunal.model.entity.Personal;
import com.credenciales.tribunal.model.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoActualRepository extends JpaRepository<EstadoActual, Integer> {
    
    List<EstadoActual> findByPersonal(Personal personal);
    
    List<EstadoActual> findByPersonalId(Long personalId);
    
    List<EstadoActual> findByEstado(Estado estado);
    
    List<EstadoActual> findByEstadoId(Integer estadoId);
    
    Optional<EstadoActual> findByPersonalIdAndEstadoId(Long personalId, Integer estadoId);
    
    @Query("SELECT ea FROM EstadoActual ea JOIN FETCH ea.estado WHERE ea.personal.id = :personalId")
    List<EstadoActual> findByPersonalIdWithEstado(@Param("personalId") Long personalId);
    
    @Query("SELECT COUNT(ea) FROM EstadoActual ea WHERE ea.estado.id = :estadoId")
    Long countByEstadoId(@Param("estadoId") Integer estadoId);
    
    @Query("SELECT ea.personal FROM EstadoActual ea WHERE ea.estado.id = :estadoId")
    List<Personal> findPersonalByEstadoId(@Param("estadoId") Integer estadoId);
    
    void deleteByPersonalId(Long personalId);
    
    boolean existsByPersonalIdAndEstadoId(Long personalId, Integer estadoId);
}