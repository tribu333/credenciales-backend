package com.credenciales.tribunal.service;

//import com.credenciales.tribunal.model.Complaint;
import com.credenciales.tribunal.model.Imagen;
//import com.credenciales.tribunal.repository.ComplaintRepository;
import com.credenciales.tribunal.repository.ImagenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import com.credenciales.tribunal.dto.image.ImagenResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImagenServiceImpl implements ImagenService {

    @Autowired
    private ImagenRepository imagenRepository;
    
/*     @Autowired
    private ComplaintRepository complaintRepository; */
    
    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public ImagenResponseDTO subirImagen(MultipartFile file, Long idComplaint) {
        // Validar que el complaint existe
        Complaint complaint = complaintRepository.findById(idComplaint)
                .orElseThrow(() -> new RuntimeException("Complaint no encontrado con ID: " + idComplaint));
        
        // Validar el archivo
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }
        
        // Validar tipo de contenido
        String contentType = file.getContentType();
        List<String> allowedMimeTypes = Arrays.asList(
        "image/jpeg",  // JPG y JPEG
            "image/jpg",   // JPG
            "image/png"    // PNG
        );
        
        if (contentType == null || !allowedMimeTypes.contains(contentType.toLowerCase())) {
            throw new RuntimeException(
                "Formato de archivo no permitido. Solo se aceptan: JPG, JPEG, PNG. " +
                "Tipo recibido: " + contentType
            );
        }
        /* if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Solo se permiten archivos de imagen (PNG, JPG, JPEG)");
        }
         */
        // Validar tamaño (opcional, ya está en application.properties pero validamos por seguridad)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("El archivo excede el tamaño máximo permitido de 5MB");
        }
        
        try {
            // Guardar archivo físicamente
            String nombreArchivo = fileStorageService.storeFile(file);
            String rutaCompleta = fileStorageService.getFilePath(nombreArchivo);
            
            // Crear entidad Imagen
            Imagen imagen = Imagen.builder()
                    .nombreArchivo(nombreArchivo)
                    .nombreOriginal(file.getOriginalFilename())
                    .rutaCompleta(rutaCompleta)
                    .mimeType(file.getContentType())
                    .tamanioBytes(file.getSize())
                    .complaint(complaint)
                    .build();
            
            // Guardar en base de datos
            Imagen imagenGuardada = imagenRepository.save(imagen);
            
            // Retornar DTO
            return convertToResponseDTO(imagenGuardada);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al subir la imagen: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ImagenResponseDTO> findById(Long id) {
        return imagenRepository.findById(id)
                .map(this::convertToResponseDTO);
    }


    @Override
    public void deleteById(Long id) {
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
        
        try {
            // Eliminar archivo físico
            fileStorageService.deleteFile(imagen.getNombreArchivo());
            
            // Eliminar de base de datos
            imagenRepository.deleteById(id);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar la imagen: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Imagen findEntityById(Long id) {
        return imagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Imagen findEntityByNombreArchivo(String nombreArchivo) {
        return imagenRepository.findByNombreArchivo(nombreArchivo)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con nombre: " + nombreArchivo));
    }

    @Override
    @Transactional(readOnly = true)
    public long contarImagenesPorComplaint(Long idComplaint) {
        Complaint complaint = complaintRepository.findById(idComplaint)
                .orElseThrow(() -> new RuntimeException("Complaint no encontrado con ID: " + idComplaint));
        
        return imagenRepository.countByComplaint(complaint);
    }

    @Override
    public void eliminarTodasImagenesComplaint(Long idComplaint) {
        Complaint complaint = complaintRepository.findById(idComplaint)
                .orElseThrow(() -> new RuntimeException("Complaint no encontrado con ID: " + idComplaint));
        
        List<Imagen> imagenes = imagenRepository.findByComplaint(complaint);
        
        // Eliminar archivos físicos
        for (Imagen imagen : imagenes) {
            try {
                fileStorageService.deleteFile(imagen.getNombreArchivo());
            } catch (Exception e) {
                // Log del error pero continuar eliminando
                System.err.println("Error al eliminar archivo: " + imagen.getNombreArchivo());
            }
        }
        
        // Eliminar de base de datos
        imagenRepository.deleteByComplaint(complaint);
    }
     @Override
    @Transactional(readOnly = true)
    public List<ImagenResponseDTO> getImagenesPorDenuncia(Long idComplaint) {
        Complaint complaint = complaintRepository.findById(idComplaint)
                .orElseThrow(() -> new RuntimeException("Complaint no encontrado con ID: " + idComplaint));
        
        return imagenRepository.findByComplaint(complaint)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    @Override
    public List<ImagenResponseDTO> subirImagenesMasivas(MultipartFile[] files, Long idComplaint) {
        // Validar que el complaint existe
        Complaint complaint = complaintRepository.findById(idComplaint)
                .orElseThrow(() -> new RuntimeException("Complaint no encontrado con ID: " + idComplaint));
        
        List<ImagenResponseDTO> imagenesSubidas = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                // Validar el archivo
                if (file.isEmpty()) {
                    System.err.println("Archivo vacío ignorado");
                    continue;
                }
                
                // Validar tipo de contenido
                String contentType = file.getContentType();
                List<String> allowedMimeTypes = Arrays.asList(
                "image/jpeg",  // JPG y JPEG
                    "image/jpg",   // JPG
                    "image/png"    // PNG
                );
                
                if (contentType == null || !allowedMimeTypes.contains(contentType.toLowerCase())) {
                    throw new RuntimeException(
                        "Formato de archivo no permitido. Solo se aceptan: JPG, JPEG, PNG. " +
                        "Tipo recibido: " + contentType
                    );
                }
                
                // Validar tamaño
                long maxSize = 5 * 1024 * 1024; // 5MB
                if (file.getSize() > maxSize) {
                    System.err.println("Archivo excede tamaño máximo: " + file.getOriginalFilename());
                    continue;
                }
                
                // Guardar archivo físicamente
                String nombreArchivo = fileStorageService.storeFile(file);
                String rutaCompleta = fileStorageService.getFilePath(nombreArchivo);
                
                // Crear entidad Imagen
                Imagen imagen = Imagen.builder()
                    .nombreArchivo(nombreArchivo)
                    .nombreOriginal(file.getOriginalFilename())
                    .rutaCompleta(rutaCompleta)
                    .mimeType(file.getContentType())
                    .tamanioBytes(file.getSize())
                    .complaint(complaint)
                    .build();
                
                // Guardar en base de datos
                Imagen imagenGuardada = imagenRepository.save(imagen);
                imagenesSubidas.add(convertToResponseDTO(imagenGuardada));
                
            } catch (Exception e) {
                System.err.println("Error al subir imagen " + file.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        return imagenesSubidas;
    }
    // ========== MÉTODOS DE CONVERSIÓN ==========
    
    private ImagenResponseDTO convertToResponseDTO(Imagen imagen) {
        ImagenResponseDTO dto = new ImagenResponseDTO();
        dto.setIdImagen(imagen.getIdImagen());
        dto.setNombreArchivo(imagen.getNombreArchivo());
        dto.setNombreOriginal(imagen.getNombreOriginal());
        dto.setMimeType(imagen.getMimeType());
        dto.setTamanioBytes(imagen.getTamanioBytes());
        dto.setTamanioFormateado(imagen.getTamanioFormateado());
        dto.setFechaSubida(imagen.getCreatedAt());
        dto.setIdComplaint(imagen.getComplaint().getId());

        
        // Generar URL de descarga
        String urlDescarga = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/imagenes/descargar/")
                .path(imagen.getNombreArchivo())
                .toUriString();
        dto.setUrlDescarga(urlDescarga);
        
        return dto;
    }
}