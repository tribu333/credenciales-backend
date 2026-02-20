package com.credenciales.tribunal.dto.image;

import com.credenciales.tribunal.model.entity.Imagen;
import org.springframework.stereotype.Component;

@Component
public class ImagenMapper {
    
    public ImagenBasicaDTO toBasicaDTO(Imagen imagen) {
        if (imagen == null) return null;
        
        return ImagenBasicaDTO.builder()
                .idImagen(imagen.getIdImagen())
                .nombreOriginal(imagen.getNombreOriginal())
                .urlDescarga(imagen.getRutaCompleta())
                .build();
    }
}