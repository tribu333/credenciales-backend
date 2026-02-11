package com.credenciales.tribunal.service;

//import com.registro.denuncias.model.Complaint;
import com.credenciales.tribunal.model.Imagen;
//import com.registro.denuncias.repository.ComplaintRepository;
import com.credenciales.tribunal.repository.ImagenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import com.credenciales.tribunal.dto.image.ImagenResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ImagenService {
    
    // Operaciones CRUD
    ImagenResponseDTO subirImagen(MultipartFile file, Long idComplaint);
    Optional<ImagenResponseDTO> findById(Long id);
    List<ImagenResponseDTO> getImagenesPorDenuncia(Long idComplaint);
    void deleteById(Long id);
    
    // Operaciones específicas
    Imagen findEntityById(Long id);
    Imagen findEntityByNombreArchivo(String nombreArchivo);
    long contarImagenesPorComplaint(Long idComplaint);
    void eliminarTodasImagenesComplaint(Long idComplaint);
    // Subir múltiples imágenes para un complaint
    List<ImagenResponseDTO> subirImagenesMasivas(MultipartFile[] files, Long idComplaint);
}
