package com.credenciales.tribunal.repository;

import com.credenciales.tribunal.model.entity.Externo;
import com.credenciales.tribunal.model.enums.TipoExterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExternoRepository extends JpaRepository<Externo, Long> {
    
    Optional<Externo> findByCarnetIdentidad(String carnetIdentidad);
    
    List<Externo> findByIdentificador(String identificador);
    
    List<Externo> findByTipoExterno(TipoExterno tipoExterno);
    
    List<Externo> findByOrgPoliticaContainingIgnoreCase(String orgPolitica);
    // Búsqueda por identificador que contenga el texto (búsqueda parcial)
    List<Externo> findByIdentificadorContainingIgnoreCase(String identificador);
    @Query("SELECT DISTINCT e FROM Externo e " +
           "LEFT JOIN FETCH e.imagen " +
           "LEFT JOIN FETCH e.asignaciones")
    List<Externo> findAllWithImagenAndAsignaciones();
    
    @Query("SELECT e FROM Externo e " +
           "LEFT JOIN FETCH e.imagen " +
           "LEFT JOIN FETCH e.asignaciones " +
           "WHERE e.id = :id")
    Externo findByIdWithImagenAndAsignaciones(Long id);
}