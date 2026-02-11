package com.credenciales.tribunal.service;

import com.credenciales.tribunal.config.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio donde se almacenarán los archivos.", ex);
        }
    }

    /**
     * Guarda un archivo en el sistema de archivos
     * @param file Archivo a guardar
     * @return Nombre del archivo guardado (UUID)
     */
    public String storeFile(MultipartFile file) {
        // Validar el archivo
        if (file.isEmpty()) {
            throw new RuntimeException("No se puede guardar un archivo vacío");
        }

        // Validar tipo de archivo (solo imágenes)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Solo se permiten archivos de imagen");
        }

        // Obtener extensión del archivo
        String nombreOriginal = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int i = nombreOriginal.lastIndexOf('.');
        if (i > 0) {
            extension = nombreOriginal.substring(i);
        }

        // Generar nombre único con UUID
        String nombreArchivo = UUID.randomUUID().toString() + extension;

        try {
            // Copiar archivo al directorio de almacenamiento
            Path targetLocation = this.fileStorageLocation.resolve(nombreArchivo);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return nombreArchivo;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo guardar el archivo " + nombreArchivo, ex);
        }
    }

    /**
     * Carga un archivo como Resource
     * @param nombreArchivo Nombre del archivo
     * @return Resource del archivo
     */
    public Resource loadFileAsResource(String nombreArchivo) {
        try {
            Path filePath = this.fileStorageLocation.resolve(nombreArchivo).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Archivo no encontrado: " + nombreArchivo);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Archivo no encontrado: " + nombreArchivo, ex);
        }
    }

    /**
     * Elimina un archivo del sistema
     * @param nombreArchivo Nombre del archivo a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean deleteFile(String nombreArchivo) {
        try {
            Path filePath = this.fileStorageLocation.resolve(nombreArchivo).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo eliminar el archivo: " + nombreArchivo, ex);
        }
    }

    /**
     * Obtiene la ruta completa de un archivo
     * @param nombreArchivo Nombre del archivo
     * @return Ruta completa como String
     */
    public String getFilePath(String nombreArchivo) {
        return this.fileStorageLocation.resolve(nombreArchivo).toString();
    }
}
