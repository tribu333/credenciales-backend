package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Acceso;
import com.credenciales.tribunal.model.entity.Qr;
import com.credenciales.tribunal.model.enums.TipoEventoAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccesoRepository extends JpaRepository<Acceso, Long> {
    
    List<Acceso> findByQr(Qr qr);
    
    List<Acceso> findByQrId(Long qrId);
    
    List<Acceso> findByTipoEvento(TipoEventoAcceso tipoEvento);
    
    @Query("SELECT a FROM Acceso a WHERE a.fechaHora BETWEEN :inicio AND :fin")
    List<Acceso> findByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
    
    @Query("SELECT COUNT(a) FROM Acceso a WHERE a.qr.id = :qrId AND a.tipoEvento = :tipoEvento AND DATE(a.fechaHora) = CURRENT_DATE")
    Long countEventosHoyByQrIdAndTipo(@Param("qrId") Long qrId, @Param("tipoEvento") TipoEventoAcceso tipoEvento);
}