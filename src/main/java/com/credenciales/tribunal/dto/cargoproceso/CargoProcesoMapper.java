package com.credenciales.tribunal.dto.cargoproceso;

import com.credenciales.tribunal.model.entity.Cargo;
import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import com.credenciales.tribunal.model.entity.Unidad;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CargoProcesoMapper {
    
    public CargoProcesoDTO toDTO(Cargo cargo) {
        if (cargo == null) return null;
        
        return CargoProcesoDTO.builder()
                .id(cargo.getId())
                .procesoId(cargo.getProceso() != null ? cargo.getProceso().getId() : null)
                .procesoNombre(cargo.getProceso() != null ? cargo.getProceso().getNombre() : null)
                .nombre(cargo.getNombre())
                .unidadId(cargo.getUnidad() != null ? cargo.getUnidad().getId() : null)
                .unidadNombre(cargo.getUnidad() != null ? cargo.getUnidad().getNombre() : null)
                .unidadAbreviatura(cargo.getUnidad() != null ? cargo.getUnidad().getAbreviatura() : null)
                .descripcion(cargo.getDescripcion())
                //.activo(cargo.getActivo())
                .createdAt(cargo.getCreatedAt())
                .totalHistoriales(cargo.getHistoriales() != null ? cargo.getHistoriales().size() : 0)
                .build();
            }
            
            public CargoProcesoResponseDTO toResponseDTO(Cargo cargo) {
                if (cargo == null) return null;
                
                return CargoProcesoResponseDTO.builder()
                .id(cargo.getId())
                .procesoId(cargo.getProceso() != null ? cargo.getProceso().getId() : null)
                .nombre(cargo.getNombre())
                .unidadId(cargo.getUnidad() != null ? cargo.getUnidad().getId() : null)
                .descripcion(cargo.getDescripcion())
                //.activo(cargo.getActivo())
                .build();
    }
    
    public Cargo toEntity(CargoProcesoCreateRequestDTO requestDTO, 
                                 ProcesoElectoral proceso, 
                                 Unidad unidad) {
        if (requestDTO == null) return null;
        
        return Cargo.builder()
                .proceso(proceso)
                .nombre(requestDTO.getNombre())
                .unidad(unidad)
                .descripcion(requestDTO.getDescripcion())
                //.activo(requestDTO.getActivo())
                .build();
    }
    
    public void updateEntity(CargoProcesoUpdateRequestDTO requestDTO, 
                            Cargo cargo,
                            Unidad unidad) {
        if (requestDTO == null || cargo == null) return;
        
        if (requestDTO.getNombre() != null) {
            cargo.setNombre(requestDTO.getNombre());
        }
        
        if (requestDTO.getDescripcion() != null) {
            cargo.setDescripcion(requestDTO.getDescripcion());
        }
        
        // if (requestDTO.getActivo() != null) {
        //     cargo.setActivo(requestDTO.getActivo());
        // }
        
        if (requestDTO.getUnidadId() != null && unidad != null) {
            cargo.setUnidad(unidad);
        }
    }
    
    
    public List<CargoProcesoDTO> toDTOList(List<Cargo> cargosProceso) {
        return cargosProceso.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<CargoProcesoResponseDTO> toResponseDTOList(List<Cargo> cargosProceso) {
        return cargosProceso.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}