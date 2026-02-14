package com.credenciales.tribunal.dto.cargoproceso;

/* import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoCreateRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoUpdateRequestDTO;
import com.credenciales.tribunal.dto.cargoproceso.CargoProcesoResponseDTO;
import com.credenciales.tribunal.dto.cargoproceso.HistorialResumenDTO; */
import com.credenciales.tribunal.model.entity.CargoProceso;
import com.credenciales.tribunal.model.entity.HistorialCargoProceso;
import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import com.credenciales.tribunal.model.entity.Unidad;
import org.springframework.stereotype.Component;
import java.util.Comparator;
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
        
        List<HistorialResumenDTO> ultimosHistoriales = null;
        if (cargoProceso.getHistoriales() != null && !cargoProceso.getHistoriales().isEmpty()) {
            ultimosHistoriales = cargoProceso.getHistoriales().stream()
                    .sorted(Comparator.comparing(HistorialCargoProceso::getFechaInicio).reversed())
                    .limit(5)
                    .map(this::toHistorialResumenDTO)
                    .collect(Collectors.toList());
        }
        
        return CargoProcesoResponseDTO.builder()
                .id(cargoProceso.getId())
                .procesoId(cargoProceso.getProceso() != null ? cargoProceso.getProceso().getId() : null)
                .procesoNombre(cargoProceso.getProceso() != null ? cargoProceso.getProceso().getNombre() : null)
                .procesoFechaInicio(cargoProceso.getProceso() != null ? 
                        cargoProceso.getProceso().getFechaInicio().atStartOfDay() : null)
                .procesoFechaFin(cargoProceso.getProceso() != null ? 
                        cargoProceso.getProceso().getFechaFin().atStartOfDay() : null)
                .procesoActivo(cargoProceso.getProceso() != null ? cargoProceso.getProceso().getEstado() : null)
                .nombre(cargoProceso.getNombre())
                .unidadId(cargoProceso.getUnidad() != null ? cargoProceso.getUnidad().getId() : null)
                .unidadNombre(cargoProceso.getUnidad() != null ? cargoProceso.getUnidad().getNombre() : null)
                .unidadAbreviatura(cargoProceso.getUnidad() != null ? cargoProceso.getUnidad().getAbreviatura() : null)
                .descripcion(cargoProceso.getDescripcion())
                //.activo(cargoProceso.getActivo())
                .createdAt(cargoProceso.getCreatedAt())
                .totalHistoriales(cargoProceso.getHistoriales() != null ? cargoProceso.getHistoriales().size() : 0)
                .ultimosHistoriales(ultimosHistoriales)
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
    
    private HistorialResumenDTO toHistorialResumenDTO(HistorialCargoProceso historial) {
        if (historial == null) return null;
        
        return HistorialResumenDTO.builder()
                .id(historial.getId())
                .personalNombre(historial.getPersonal() != null ? historial.getPersonal().getNombre() : null)
                .personalApellido(historial.getPersonal() != null ? historial.getPersonal().getApellidoPaterno() : null)
                .personalApellido(historial.getPersonal() != null ? historial.getPersonal().getApellidoMaterno() : null)
                .fechaInicio(historial.getFechaInicio())
                .fechaFin(historial.getFechaFin())
                .activo(historial.getActivo())
                .build();
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