package com.credenciales.tribunal.repository;

//import com.registro.denuncias.model.Complaint;
import com.credenciales.tribunal.model.entity.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Long> {
    
    
    void deleteByNombreArchivo(String nombreArchivo);

    Optional<Imagen> findByNombreArchivo(String nombreArchivo);
    
    // Buscar por nombre original (exact match)
    List<Imagen> findByNombreOriginal(String nombreOriginal);
    // Buscar imágenes que contengan cierto texto en el nombre original
    List<Imagen> findByNombreOriginalContainingIgnoreCase(String nombreOriginal);
    
    // Buscar imágenes que empiecen con cierto texto
    //List<Imagen> findByNombreOriginalStartingWith(String prefijo);

    @Query("SELECT i FROM Imagen i WHERE i.nombreOriginal LIKE :nombreBase%")
    List<Imagen> findByNombreOriginalStartingWith(@Param("nombreBase") String nombreBase);
}