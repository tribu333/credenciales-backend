package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Externo;
import com.credenciales.tribunal.model.enums.TipoExterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExternoRepository extends JpaRepository<Externo, Long> {
    
    Optional<Externo> findByCarnetIdentidad(String carnetIdentidad);
    
    Optional<Externo> findByIdentificador(String identificador);
    
    List<Externo> findByTipoExterno(TipoExterno tipoExterno);
    
    List<Externo> findByOrgPoliticaContainingIgnoreCase(String orgPolitica);
}