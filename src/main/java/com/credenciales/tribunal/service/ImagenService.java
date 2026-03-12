package com.credenciales.tribunal.service;

import com.credenciales.tribunal.model.entity.Imagen;
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
    ImagenResponseDTO subirImagen(MultipartFile file);
    Optional<ImagenResponseDTO> findById(Long id);
    void deleteById(Long id);
    
    // Operaciones específicas
    Imagen findEntityById(Long id);
    Imagen findEntityByNombreArchivo(String nombreArchivo);
    
    List<ImagenResponseDTO> subirImagenesMasivas(MultipartFile[] files);

    Optional<ImagenResponseDTO> findByNombreOriginal(String nombreOriginal);
    List<ImagenResponseDTO> findAllByNombreOriginalContaining(String texto);
    Optional<ImagenResponseDTO> findByNombreBase(String nombreBase);
}
