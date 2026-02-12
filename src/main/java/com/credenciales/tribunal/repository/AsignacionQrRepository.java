package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.AsignacionQr;
import com.credenciales.tribunal.model.entity.Externo;
import com.credenciales.tribunal.model.entity.Qr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionQrRepository extends JpaRepository<AsignacionQr, Long> {
    
    List<AsignacionQr> findByExterno(Externo externo);
    
    List<AsignacionQr> findByExternoId(Long externoId);
    
    Optional<AsignacionQr> findByQrIdAndActivoTrue(Long qrId);
    
    List<AsignacionQr> findByActivoTrue();
    
    @Query("SELECT a FROM AsignacionQr a WHERE a.externo.id = :externoId AND a.activo = true")
    Optional<AsignacionQr> findActivaByExternoId(@Param("externoId") Long externoId);
}