package com.credenciales.tribunal.dto.externo;

import com.credenciales.tribunal.dto.image.ImagenMapper;
import com.credenciales.tribunal.model.entity.Externo;
import com.credenciales.tribunal.model.entity.Imagen;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExternoMapper {
    private final ImagenMapper imagenMapper = new ImagenMapper();
    private final AsignacionQrMapper asignacionQrMapper = new AsignacionQrMapper();
    public ExternoDTO toDTO(Externo externo) {
        if (externo == null) return null;
        
        return ExternoDTO.builder()
                .id(externo.getId())
                .nombreCompleto(externo.getNombreCompleto())
                .carnetIdentidad(externo.getCarnetIdentidad())
                .identificador(externo.getIdentificador())
                .orgPolitica(externo.getOrgPolitica())
                .tipoExterno(externo.getTipoExterno())
                .createdAt(externo.getCreatedAt())
                .imagenId(externo.getImagen() != null ? externo.getImagen().getIdImagen() : null)
                .imagenNombre(externo.getImagen() != null ? externo.getImagen().getNombreArchivo() : null)
                .imagenUrl(externo.getImagen() != null ? externo.getImagen().getRutaCompleta() : null)
                .build();
    }
    
    public ExternoResponseDTO toResponseDTO(Externo externo) {
        if (externo == null) return null;
        
        return ExternoResponseDTO.builder()
                .id(externo.getId())
                .nombreCompleto(externo.getNombreCompleto())
                .carnetIdentidad(externo.getCarnetIdentidad())
                .identificador(externo.getIdentificador())
                .orgPolitica(externo.getOrgPolitica())
                .tipoExterno(externo.getTipoExterno())
                .createdAt(externo.getCreatedAt())
                .imagenId(externo.getImagen() != null ? externo.getImagen().getIdImagen() : null)
                .imagenNombre(externo.getImagen() != null ? externo.getImagen().getNombreOriginal() : null)
                .imagenUrl(externo.getImagen() != null ? externo.getImagen().getRutaCompleta() : null)
                .totalAsignaciones(externo.getAsignaciones() != null ? externo.getAsignaciones().size() : 0)
                .build();
    }
    // NUEVO MÉTODO: toDetalleResponseDTO
    public ExternoDetalleResponseDTO toDetalleResponseDTO(Externo externo) {
        if (externo == null) return null;
        
        return ExternoDetalleResponseDTO.builder()
                .id(externo.getId())
                .nombreCompleto(externo.getNombreCompleto())
                .carnetIdentidad(externo.getCarnetIdentidad())
                .identificador(externo.getIdentificador())
                .orgPolitica(externo.getOrgPolitica())
                .tipoExterno(externo.getTipoExterno())
                .createdAt(externo.getCreatedAt())
                // Usamos el ImagenMapper para convertir la imagen
                .imagen(imagenMapper.toBasicaDTO(externo.getImagen()))
                // Usamos el AsignacionQrMapper para convertir la lista de asignaciones
                .asignaciones(asignacionQrMapper.toBasicaDTOList(externo.getAsignaciones()))
                .build();
    }

    public Externo toEntity(ExternoRequestDTO requestDTO, Imagen imagen) {
        if (requestDTO == null) return null;
        
        return Externo.builder()
                .nombreCompleto(requestDTO.getNombreCompleto())
                .carnetIdentidad(requestDTO.getCarnetIdentidad())
                .identificador(requestDTO.getIdentificador())
                .orgPolitica(requestDTO.getOrgPolitica())
                .tipoExterno(requestDTO.getTipoExterno())
                .imagen(imagen)
                .build();
    }
    
    public void updateEntity(ExternoRequestDTO requestDTO, Externo externo, Imagen imagen) {
        if (requestDTO == null || externo == null) return;
        
        externo.setNombreCompleto(requestDTO.getNombreCompleto());
        externo.setCarnetIdentidad(requestDTO.getCarnetIdentidad());
        externo.setIdentificador(requestDTO.getIdentificador());
        externo.setOrgPolitica(requestDTO.getOrgPolitica());
        externo.setTipoExterno(requestDTO.getTipoExterno());
        
        if (imagen != null) {
            externo.setImagen(imagen);
        }
    }
    
    public List<ExternoDTO> toDTOList(List<Externo> externos) {
        return externos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<ExternoResponseDTO> toResponseDTOList(List<Externo> externos) {
        return externos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // NUEVO MÉTODO: para listas de detalles (opcional)
    public List<ExternoDetalleResponseDTO> toDetalleResponseDTOList(List<Externo> externos) {
        return externos.stream()
                .map(this::toDetalleResponseDTO)
                .collect(Collectors.toList());
    }
}