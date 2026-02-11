package com.credenciales.tribunal.repository;

//import com.registro.denuncias.model.Complaint;
import com.credenciales.tribunal.model.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Long> {
    // Buscar imágenes por complaint
    //List<Imagen> findByComplaint(Complaint complaint);
    List<Imagen> findByComplaintId(Long id);
    
    void deleteByNombreArchivo(String nombreArchivo);

    Optional<Imagen> findByNombreArchivo(String nombreArchivo);
    /* Long countByComplaint(Complaint complaint);
    // Eliminar todas las imágenes de un complaint
    void deleteByComplaint(Complaint complaint); */

}