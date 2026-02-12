package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProcesoElectoralRepository extends JpaRepository<ProcesoElectoral, Long> {
    
    List<ProcesoElectoral> findByEstadoTrue();
    
    List<ProcesoElectoral> findByNombreContainingIgnoreCase(String nombre);
    
    @Query("SELECT p FROM ProcesoElectoral p WHERE p.fechaInicio <= :fecha AND p.fechaFin >= :fecha AND p.estado = true")
    List<ProcesoElectoral> findActivosEnFecha(@Param("fecha") LocalDate fecha);
    
    @Query("SELECT p FROM ProcesoElectoral p WHERE p.fechaFin < :fecha")
    List<ProcesoElectoral> findFinalizadosAntesDe(@Param("fecha") LocalDate fecha);
}