package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Qr;
import com.credenciales.tribunal.model.enums.EstadoQr;
import com.credenciales.tribunal.model.enums.TipoQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QrRepository extends JpaRepository<Qr, Long> {
    
    Optional<Qr> findByCodigo(String codigo);
    
    List<Qr> findByEstado(EstadoQr estado);
    
    List<Qr> findByTipoAndEstado(TipoQr tipo, EstadoQr estado);
    
    @Query("SELECT q FROM Qr q WHERE q.estado = :estado AND q.tipo = :tipo ORDER BY q.createdAt DESC")
    List<Qr> findLatestByTipoAndEstado(@Param("tipo") TipoQr tipo, @Param("estado") EstadoQr estado);
    
    @Query("SELECT q FROM Qr q WHERE q.personal.id = :personalId")
    Optional<Qr> findByPersonalId(@Param("personalId") Long personalId);
    
    boolean existsByCodigo(String codigo);
    
    @Modifying
    @Query("UPDATE Qr q SET q.estado = :estado WHERE q.id = :id")
    void updateEstado(@Param("id") Long id, @Param("estado") EstadoQr estado);
    
    @Query("SELECT COUNT(q) FROM Qr q WHERE q.estado = :estado")
    long countByEstado(@Param("estado") EstadoQr estado);
}