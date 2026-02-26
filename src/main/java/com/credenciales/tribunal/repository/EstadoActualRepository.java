package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.EstadoActual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoActualRepository extends JpaRepository<EstadoActual, Long> {

    List<EstadoActual> findByPersonalId(Long personalId);

    @Query("SELECT ea FROM EstadoActual ea WHERE ea.personal.id = :personalId AND ea.valor_estado_actual = true")
    Optional<EstadoActual> findCurrentEstadoByPersonalId(@Param("personalId") Long personalId);

    @Query("SELECT ea FROM EstadoActual ea WHERE ea.personal.id = :personalId ORDER BY ea.createdAt DESC")
    List<EstadoActual> findHistorialByPersonalId(@Param("personalId") Long personalId);

    @Query("SELECT ea FROM EstadoActual ea WHERE ea.estado.nombre = :estadoNombre AND ea.valor_estado_actual = true")
    List<EstadoActual> findAllByCurrentEstado(@Param("estadoNombre") String estadoNombre);

    @Query("SELECT CASE WHEN COUNT(ea) > 0 THEN true ELSE false END FROM EstadoActual ea " +
            "WHERE ea.personal.id = :personalId AND ea.estado.nombre = :estadoNombre AND ea.valor_estado_actual = true")
    boolean existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
            @Param("personalId") Long personalId,
            @Param("estadoNombre") String estadoNombre);

    /**
     * Desactiva (set valor_estado_actual = false) todos los estados actuales activos
     * para una lista de IDs de personal.
     */
    @Modifying
    @Transactional
    @Query("UPDATE EstadoActual ea SET ea.valor_estado_actual = false " +
            "WHERE ea.personal.id IN :personalIds AND ea.valor_estado_actual = true")
    int bulkDesactivarEstadosActuales(@Param("personalIds") List<Long> personalIds);

    /**
     * Encuentra todos los estados actuales activos para una lista de personalIds.
     */
    @Query("SELECT ea FROM EstadoActual ea WHERE ea.personal.id IN :personalIds AND ea.valor_estado_actual = true")
    List<EstadoActual> findAllCurrentEstadosByPersonalIds(@Param("personalIds") List<Long> personalIds);

    /**
     * Encuentra todos los estados actuales activos filtrando por nombre de estado.
     */
    @Query("SELECT ea FROM EstadoActual ea " +
            "WHERE ea.personal.id IN :personalIds " +
            "AND ea.valor_estado_actual = true " +
            "AND ea.estado.nombre = :estadoNombre")
    List<EstadoActual> findAllCurrentEstadosByPersonalIdsAndEstado(
            @Param("personalIds") List<Long> personalIds,
            @Param("estadoNombre") String estadoNombre);

    /**
     * Cuenta cuántos personales tienen un estado específico activo.
     */
    @Query("SELECT COUNT(ea) FROM EstadoActual ea " +
            "WHERE ea.estado.nombre = :estadoNombre AND ea.valor_estado_actual = true")
    long countByEstadoNombreAndValorEstadoActualTrue(@Param("estadoNombre") String estadoNombre);
}