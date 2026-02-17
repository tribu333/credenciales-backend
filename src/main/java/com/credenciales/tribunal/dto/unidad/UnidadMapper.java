package com.credenciales.tribunal.dto.unidad;

import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoResponseDTO;
import com.credenciales.tribunal.model.entity.Unidad;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UnidadMapper {
    
    public UnidadDTO toDTO(Unidad unidad) {
        if (unidad == null) return null;
        
        return UnidadDTO.builder()
                .id(unidad.getId())
                .nombre(unidad.getNombre())
                .abreviatura(unidad.getAbreviatura())
                .estado(unidad.getEstado())
                .createdAt(unidad.getCreatedAt())
                .build();
    }
    
    public UnidadResponseDTO toResponseDTO(Unidad unidad) {
        if (unidad == null) return null;
        
        return UnidadResponseDTO.builder()
                .id(unidad.getId())
                .nombre(unidad.getNombre())
                .abreviatura(unidad.getAbreviatura())
                .estado(unidad.getEstado())
                .createdAt(unidad.getCreatedAt())
                .totalCargos(unidad.getCargos() != null ? unidad.getCargos().size() : 0)
                .totalCargosProceso(unidad.getCargosProceso() != null ? unidad.getCargosProceso().size() : 0)
                .build();
    }
   /*  public UnidadResponseDTO toResponseCompDTO(Unidad unidad) {
        if (unidad == null) return null;
        
        return UnidadResponseCmpDTO.builder()
                .id(unidad.getId())
                .nombre(unidad.getNombre())
                .abreviatura(unidad.getAbreviatura())
                .estado(unidad.getEstado())
                .createdAt(unidad.getCreatedAt())
                .totalCargos(unidad.getCargos() != null ? unidad.getCargos().size() : 0)
                .totalCargosProceso(unidad.getCargosProceso() != null ? unidad.getCargosProceso().size() : 0)
                .procesos(CargoProcesoResponseDTO.builder()
                .id(unidad.getCargos())
            )
                .build();
    } */
    
    public Unidad toEntity(UnidadRequestDTO requestDTO) {
        if (requestDTO == null) return null;
        
        return Unidad.builder()
                .nombre(requestDTO.getNombre())
                .abreviatura(requestDTO.getAbreviatura())
                .estado(requestDTO.getEstado())
                .build();
    }
    
    public void updateEntity(UnidadRequestDTO requestDTO, Unidad unidad) {
        if (requestDTO == null || unidad == null) return;
        
        unidad.setNombre(requestDTO.getNombre());
        unidad.setAbreviatura(requestDTO.getAbreviatura());
        unidad.setEstado(requestDTO.getEstado());
    }
    
    public List<UnidadDTO> toDTOList(List<Unidad> unidades) {
        return unidades.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<UnidadResponseDTO> toResponseDTOList(List<Unidad> unidades) {
        return unidades.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}