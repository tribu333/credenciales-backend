package com.credenciales.tribunal.dto.cargoproceso;

/* import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoCreateRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoUpdateRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoResponseDTO;
import com.credenciales.tribunal.dto.cargoproceso.HistorialResumenDTO; */
import com.credenciales.tribunal.model.entity.CargoProceso;
import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import com.credenciales.tribunal.model.entity.Unidad;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CargoProcesoMapper {
    
    public CargoProcesoDTO toDTO(CargoProceso cargoProceso) {
        if (cargoProceso == null) return null;
        
        return CargoProcesoDTO.builder()
                .id(cargoProceso.getId())
                .procesoId(cargoProceso.getProceso() != null ? cargoProceso.getProceso().getId() : null)
                .procesoNombre(cargoProceso.getProceso() != null ? cargoProceso.getProceso().getNombre() : null)
                .nombre(cargoProceso.getNombre())
                .unidadId(cargoProceso.getUnidad() != null ? cargoProceso.getUnidad().getId() : null)
                .unidadNombre(cargoProceso.getUnidad() != null ? cargoProceso.getUnidad().getNombre() : null)
                .unidadAbreviatura(cargoProceso.getUnidad() != null ? cargoProceso.getUnidad().getAbreviatura() : null)
                .descripcion(cargoProceso.getDescripcion())
                //.activo(cargoProceso.getActivo())
                .createdAt(cargoProceso.getCreatedAt())
                .totalHistoriales(cargoProceso.getHistoriales() != null ? cargoProceso.getHistoriales().size() : 0)
                .build();
    }
    
    public CargoProcesoResponseDTO toResponseDTO(CargoProceso cargoProceso) {
        if (cargoProceso == null) return null;
        
        return CargoProcesoResponseDTO.builder()
                .id(cargoProceso.getId())
                .procesoNombre(cargoProceso.getProceso() != null ? cargoProceso.getProceso().getNombre() : null)
                .nombre(cargoProceso.getNombre())
                .unidadId(cargoProceso.getUnidad() != null ? cargoProceso.getUnidad().getId() : null)
                .unidadNombre(cargoProceso.getUnidad() != null ? cargoProceso.getUnidad().getNombre() : null)
                .unidadAbreviatura(cargoProceso.getUnidad() != null ? cargoProceso.getUnidad().getAbreviatura() : null)
                .descripcion(cargoProceso.getDescripcion())
                .build();
    }
    
    public CargoProceso toEntity(CargoProcesoCreateRequestDTO requestDTO, 
                                 ProcesoElectoral proceso, 
                                 Unidad unidad) {
        if (requestDTO == null) return null;
        
        return CargoProceso.builder()
                .proceso(proceso)
                .nombre(requestDTO.getNombre())
                .unidad(unidad)
                .descripcion(requestDTO.getDescripcion())
                //.activo(requestDTO.getActivo())
                .build();
    }
    
    public void updateEntity(CargoProcesoUpdateRequestDTO requestDTO, 
                            CargoProceso cargoProceso,
                            Unidad unidad) {
        if (requestDTO == null || cargoProceso == null) return;
        
        if (requestDTO.getNombre() != null) {
            cargoProceso.setNombre(requestDTO.getNombre());
        }
        
        if (requestDTO.getDescripcion() != null) {
            cargoProceso.setDescripcion(requestDTO.getDescripcion());
        }
        
        // if (requestDTO.getActivo() != null) {
        //     cargoProceso.setActivo(requestDTO.getActivo());
        // }
        
        if (requestDTO.getUnidadId() != null && unidad != null) {
            cargoProceso.setUnidad(unidad);
        }
    }
    
    
    public List<CargoProcesoDTO> toDTOList(List<CargoProceso> cargosProceso) {
        return cargosProceso.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<CargoProcesoResponseDTO> toResponseDTOList(List<CargoProceso> cargosProceso) {
        return cargosProceso.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}