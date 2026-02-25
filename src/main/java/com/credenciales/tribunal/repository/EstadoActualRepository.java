package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.EstadoActual;
import com.credenciales.tribunal.model.entity.Personal;
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

    // CORREGIDO: Usar @Query en lugar de nombre de método problemático
    @Query("SELECT CASE WHEN COUNT(ea) > 0 THEN true ELSE false END FROM EstadoActual ea " +
            "WHERE ea.personal.id = :personalId AND ea.estado.nombre = :estadoNombre AND ea.valor_estado_actual = true")
    boolean existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
            @Param("personalId") Long personalId,
            @Param("estadoNombre") String estadoNombre);

    // También puedes crear esta versión alternativa si prefieres
    default boolean existsByPersonalIdAndEstadoActualTrue(Long personalId, String estadoNombre) {
        return existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(personalId, estadoNombre);
    }

    /**
     * Desactiva (set valor_estado_actual = false) todos los estados actuales activos
     * para una lista de IDs de personal.
     * ESTO ES CRÍTICO PARA OPERACIONES MASIVAS Y PARA EVITAR CONDICIONES DE CARRERA.
     */
    @Modifying
    @Transactional
    @Query("UPDATE EstadoActual ea SET ea.valor_estado_actual = false " +
            "WHERE ea.personal.id IN :personalIds AND ea.valor_estado_actual = true")
    int bulkDesactivarEstadosActuales(@Param("personalIds") List<Long> personalIds);

    /**
     * Encuentra todos los estados actuales activos para una lista de personalIds.
     * Útil para lógica de validación.
     */
    @Query("SELECT ea FROM EstadoActual ea WHERE ea.personal.id IN :personalIds AND ea.valor_estado_actual = true")
    List<EstadoActual> findAllCurrentEstadosByPersonalIds(@Param("personalIds") List<Long> personalIds);


}