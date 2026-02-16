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
    
    Optional<Personal> findByCorreo(String correo);
    
    Optional<Personal> findByQrId(Long qrId);
    
    List<Personal> findByTipo(TipoPersonal tipo);
    
    @Query("SELECT p FROM Personal p WHERE p.accesoComputo = true")
    List<Personal> findAllWithAccesoComputo();
    
    @Query("SELECT p FROM Personal p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(p.apellidoPaterno) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Personal> buscarPorNombre(@Param("nombre") String nombre);
}