package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Qr;
import com.credenciales.tribunal.model.enums.EstadoQr;
import com.credenciales.tribunal.model.enums.TipoQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrRepository extends JpaRepository<Qr, Long> {
    
    Optional<Qr> findByCodigo(String codigo);
    
    List<Qr> findByEstado(EstadoQr estado);
    
    List<Qr> findByTipo(TipoQr tipo);
    
    List<Qr> findByEstadoAndTipo(EstadoQr estado, TipoQr tipo);
    
    boolean existsByCodigo(String codigo);
}