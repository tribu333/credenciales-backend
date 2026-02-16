package com.credenciales.tribunal.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageOptimizationService {

    /**
     * Optimiza la imagen reduciendo su tamaño y calidad
     */
    public byte[] optimizeImage(MultipartFile file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // Determinar formato y calidad según tipo
        String format = getFormat(file.getContentType());
        double quality = getQualityBySize(file.getSize());
        
        // Redimensionar si es necesario
        Thumbnails.of(file.getInputStream())
                .size(1920, 1080) // Tamaño máximo HD
                .outputFormat(format)
                .outputQuality(quality)
                .toOutputStream(outputStream);
        
        return outputStream.toByteArray();
    }
    
    /**
     * Versión personalizable
     */
    public byte[] optimizeImage(MultipartFile file, int width, int height, double quality) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String format = getFormat(file.getContentType());
        
        Thumbnails.of(file.getInputStream())
                .size(width, height)
                .outputFormat(format)
                .outputQuality(quality)
                .toOutputStream(outputStream);
        
        return outputStream.toByteArray();
    }
    
    private String getFormat(String contentType) {
        if (contentType == null) return "jpg";
        
        switch (contentType) {
            case "image/png": return "png";
            case "image/jpeg":
            case "image/jpg":
            default: return "jpg";
        }
    }
    
    private double getQualityBySize(long sizeInBytes) {
        long sizeInMB = sizeInBytes / (1024 * 1024);
        
        if (sizeInMB > 5) return 0.3; // >5MB: calidad baja
        if (sizeInMB > 2) return 0.5; // >2MB: calidad media
        if (sizeInMB > 1) return 0.7; // >1MB: calidad media-alta
        return 0.9; // <1MB: calidad alta
    }
}
