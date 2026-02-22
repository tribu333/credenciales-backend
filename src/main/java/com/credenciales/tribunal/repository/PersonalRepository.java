package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Personal;
import com.credenciales.tribunal.model.enums.TipoPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalRepository extends JpaRepository<Personal, Long> {
    
    Optional<Personal> findByCarnetIdentidad(String carnetIdentidad);
    
    List<Personal> findAllByCarnetIdentidad(String carnetIdentidad);
    
    Optional<Personal> findByCorreo(String correo);

    List<Personal> findAllByCorreo(String correo);

    Optional<Personal> findByQrId(Long qrId);
    
    List<Personal> findByTipo(TipoPersonal tipo);
    
    @Query("SELECT p FROM Personal p WHERE p.accesoComputo = true")
    List<Personal> findAllWithAccesoComputo();
    
    @Query("SELECT p FROM Personal p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(p.apellidoPaterno) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Personal> buscarPorNombre(@Param("nombre") String nombre);

    @Query("SELECT p FROM Personal p JOIN p.estadosActuales ea WHERE ea.estado.nombre = :estadoNombre AND ea.valor_estado_actual = true")
    List<Personal> findAllByCurrentEstado(@Param("estadoNombre") String estadoNombre);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Personal p " +
        "JOIN p.estadosActuales ea " +
        "WHERE p.id = :personalId AND ea.estado.nombre = :estadoNombre AND ea.valor_estado_actual = true")
    boolean isPersonalInEstado(@Param("personalId") Long personalId, @Param("estadoNombre") String estadoNombre);

    List<Personal> findByNroCircunscripcionContainingIgnoreCase(String nroCircunscripcion);

    List<Personal> findByNroCircunscripcion(String nroCircunscripcion);

    @Query("SELECT DISTINCT p FROM Personal p " +
            "LEFT JOIN FETCH p.imagen " +
            "LEFT JOIN FETCH p.qr " +
            "LEFT JOIN FETCH p.historialCargosProceso hcp " +
            "LEFT JOIN FETCH hcp.cargoProceso cp " +
            "LEFT JOIN FETCH cp.unidad u " +
            "LEFT JOIN FETCH p.estadosActuales ea " +
            "LEFT JOIN FETCH ea.estado e " +
            "WHERE (hcp.activo = true OR hcp IS NULL) " +
            "AND (ea.valor_estado_actual = true OR ea IS NULL)")
    List<Personal> findAllConTodoCargado();
}