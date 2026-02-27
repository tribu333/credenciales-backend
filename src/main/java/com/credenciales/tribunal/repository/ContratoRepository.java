package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Contrato;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    // Buscar contratos por personal
    List<Contrato> findByPersonalId(Long personalId);

    List<Contrato> findByCargoId(long cargoId);

    // Buscar contratos activos
    List<Contrato> findByActivoTrue();

    // Buscar contratos activo de un personal
    List<Contrato> findByPersonalIdAndActivoTrue(Long personalId);

    // Verificar si un personal ya tiene contrato activo
    boolean existsByPersonalIdAndActivoTrue(Long personalId);

    // Buscar contratos por cargo
    List<Contrato> findByCargoId(Long cargoId);

    // Buscar contratos por proceso
    List<Contrato> findByProcesoId(Long procesoId);

    // Buscar por rango de fechas
    List<Contrato> findByFechaInicioBetween(LocalDateTime inicio, LocalDateTime fin);

    /* @Query("")
    List<Contrato> findByPersonalIdWithCargoProceso (@Param("procesoId") Long procesoId); */

}