package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.dto.personal.PersonalCertificadoDTO;
import com.credenciales.tribunal.dto.personal.PersonalDetallesDTO;
import com.credenciales.tribunal.model.entity.Personal;
import com.credenciales.tribunal.model.enums.TipoPersonal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
            "AND (ea.valor_estado_actual = true OR ea IS NULL)" +
            "ORDER BY p.createdAt DESC")
    List<Personal> findAllConTodoCargado();

    @Query(value = "SELECT * FROM personal WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Personal> findPersonalByIdWithPessimisticLock(@Param("id") Long id);

    @Query("SELECT p FROM Personal p WHERE p.id IN :ids")
    List<Personal> findAllById(@Param("ids") List<Long> ids);

    @Query(value = """
            SELECT
                p.id as id,
                p.nombre as nombre,
                p.apellido_paterno as apellidoPaterno,
                p.apellido_materno as apellidoMaterno,
                p.carnet_identidad as carnetIdentidad,
                e.nombre as estadoActual,
                cp.nombre as cargo,
                '' as descripcion,
                pe.nombre as proceso,
                DATE_FORMAT(hcp.fecha_inicio, '%Y-%m-%d') as fecha_ini,
                DATE_FORMAT(hcp.fecha_fin, '%Y-%m-%d') as fecha_fin
            FROM personal p
            LEFT JOIN historial_cargo_proceso hcp ON p.id = hcp.personal_id AND hcp.activo = true
            LEFT JOIN cargo_proceso cp ON hcp.cargo_proceso_id = cp.id
            LEFT JOIN proceso_electoral pe ON cp.proceso_id = pe.id
            LEFT JOIN estado_actual ea ON p.id = ea.personal_id AND ea.valor_estado_actual = true
            LEFT JOIN estado e ON ea.estado_id = e.id
            WHERE p.id IN (:ids)
            AND e.nombre = 'INACTIVO PROCESO ELECTORAL TERMINADO'
            ORDER BY p.apellido_paterno, p.apellido_materno, p.nombre
            """, nativeQuery = true)
    List<Object[]> findCertificadosDataNative(@Param("ids") List<Long> ids);

        @Query("SELECT DISTINCT p FROM Personal p " +
            "LEFT JOIN FETCH p.imagen " +
            "LEFT JOIN FETCH p.qr " +
            "LEFT JOIN FETCH p.historialCargosProceso hcp " +
            "LEFT JOIN FETCH hcp.cargoProceso cp " +
            "LEFT JOIN FETCH cp.unidad u " +
            "LEFT JOIN FETCH p.estadosActuales ea " +
            "LEFT JOIN FETCH ea.estado e " +
            "ORDER BY p.createdAt DESC")
    List<Personal> findAllConTodo();
    //findAllConTodo
}