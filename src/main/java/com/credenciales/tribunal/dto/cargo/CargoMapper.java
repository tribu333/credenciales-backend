package com.credenciales.tribunal.dto.cargo;

import com.credenciales.tribunal.model.entity.Cargo;
import com.credenciales.tribunal.model.entity.Unidad;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CargoMapper {
    
    public CargoDTO toDTO(Cargo cargo) {
        if (cargo == null) return null;
        
        return CargoDTO.builder()
                .id(cargo.getId())
                .nombre(cargo.getNombre())
                .unidadId(cargo.getUnidad() != null ? cargo.getUnidad().getId() : null)
                .unidadNombre(cargo.getUnidad() != null ? cargo.getUnidad().getNombre() : null)
                .unidadAbreviatura(cargo.getUnidad() != null ? cargo.getUnidad().getAbreviatura() : null)
                .createdAt(cargo.getCreatedAt())
                .build();
    }
    
    public CargoResponseDTO toResponseDTO(Cargo cargo) {
        if (cargo == null) return null;
        
        return CargoResponseDTO.builder()
                .id(cargo.getId())
                .nombre(cargo.getNombre())
                .unidadId(cargo.getUnidad() != null ? cargo.getUnidad().getId() : null)
                .unidadNombre(cargo.getUnidad() != null ? cargo.getUnidad().getNombre() : null)
                .unidadAbreviatura(cargo.getUnidad() != null ? cargo.getUnidad().getAbreviatura() : null)
                .createdAt(cargo.getCreatedAt())
                .totalHistoriales(cargo.getHistoriales() != null ? cargo.getHistoriales().size() : 0)
                .build();
    }
    
    public Cargo toEntity(CargoRequestDTO requestDTO, Unidad unidad) {
        if (requestDTO == null) return null;
        
        return Cargo.builder()
                .nombre(requestDTO.getNombre())
                .unidad(unidad)
                .build();
    }
    
    public void updateEntity(CargoRequestDTO requestDTO, Cargo cargo, Unidad unidad) {
        if (requestDTO == null || cargo == null) return;
        
        cargo.setNombre(requestDTO.getNombre());
        if (unidad != null) {
            cargo.setUnidad(unidad);
        }
    }
    
    public List<CargoDTO> toDTOList(List<Cargo> cargos) {
        return cargos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<CargoResponseDTO> toResponseDTOList(List<Cargo> cargos) {
        return cargos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}