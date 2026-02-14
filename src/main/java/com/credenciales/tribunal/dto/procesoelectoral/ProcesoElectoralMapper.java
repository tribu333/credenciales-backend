package com.credenciales.tribunal.dto.procesoelectoral;

/* import com.credenciales.tribunal.dto.procesolectoral.ProcesoElectoralDTO;
import com.credenciales.tribunal.dto.procesolectoral.request.ProcesoElectoralCreateRequestDTO;
import com.credenciales.tribunal.dto.procesolectoral.request.ProcesoElectoralUpdateRequestDTO;
import com.credenciales.tribunal.dto.procesolectoral.response.CargoProcesoResumenDTO;
import com.credenciales.tribunal.dto.procesolectoral.response.ProcesoElectoralResponseDTO;
 */
import com.credenciales.tribunal.model.entity.CargoProceso;
import com.credenciales.tribunal.model.entity.Imagen;
import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcesoElectoralMapper {
    
    public ProcesoElectoralDTO toDTO(ProcesoElectoral proceso) {
        if (proceso == null) return null;
        
        return ProcesoElectoralDTO.builder()
                .id(proceso.getId())
                .nombre(proceso.getNombre())
                .descripcion(proceso.getDescripcion())
                .fechaInicio(proceso.getFechaInicio())
                .fechaFin(proceso.getFechaFin())
                .estado(proceso.getEstado())
                .imagenId(proceso.getImagen() != null ? proceso.getImagen().getIdImagen() : null)
                .imagenNombre(proceso.getImagen() != null ? proceso.getImagen().getNombreArchivo() : null)
                .imagenUrl(proceso.getImagen() != null ? proceso.getImagen().getRutaCompleta() : null)
                .createdAt(proceso.getCreatedAt())
                .updatedAt(proceso.getUpdatedAt())
                .totalCargos(proceso.getCargosProceso() != null ? proceso.getCargosProceso().size() : 0)
                .build();
    }
    
    public ProcesoElectoralResponseDTO toResponseDTO(ProcesoElectoral proceso) {
        if (proceso == null) return null;
        
        LocalDate hoy = LocalDate.now();
        String estadoDescripcion = determinarEstadoDescripcion(proceso, hoy);
        boolean vigente = hoy.isAfter(proceso.getFechaInicio()) && hoy.isBefore(proceso.getFechaFin());
        long duracionDias = ChronoUnit.DAYS.between(proceso.getFechaInicio(), proceso.getFechaFin());
        
        int totalCargosActivos = 0;
        if (proceso.getCargosProceso() != null) {
            totalCargosActivos = (int) proceso.getCargosProceso().stream()
                    .filter(CargoProceso::getActivo)
                    .count();
        }
        
        return ProcesoElectoralResponseDTO.builder()
                .id(proceso.getId())
                .nombre(proceso.getNombre())
                .descripcion(proceso.getDescripcion())
                .fechaInicio(proceso.getFechaInicio())
                .fechaFin(proceso.getFechaFin())
                .estado(proceso.getEstado())
                .estadoDescripcion(estadoDescripcion)
                .imagenId(proceso.getImagen() != null ? proceso.getImagen().getIdImagen() : null)
                .imagenNombre(proceso.getImagen() != null ? proceso.getImagen().getNombreArchivo() : null)
                .imagenUrl(proceso.getImagen() != null ? proceso.getImagen().getRutaCompleta() : null)
                .createdAt(proceso.getCreatedAt())
                .updatedAt(proceso.getUpdatedAt())
                .totalCargos(proceso.getCargosProceso() != null ? proceso.getCargosProceso().size() : 0)
                .totalCargosActivos(totalCargosActivos)
                .duracionDias(duracionDias)
                .vigente(vigente && proceso.getEstado())
                .cargos(proceso.getCargosProceso() != null ? 
                        convertirCargosProceso(proceso.getCargosProceso()) : null)
                .build();
    }
    
    public ProcesoElectoral toEntity(ProcesoElectoralCreateRequestDTO requestDTO, Imagen imagen) {
        if (requestDTO == null) return null;
        
        return ProcesoElectoral.builder()
                .nombre(requestDTO.getNombre())
                .descripcion(requestDTO.getDescripcion())
                .fechaInicio(requestDTO.getFechaInicio())
                .fechaFin(requestDTO.getFechaFin())
                .estado(requestDTO.getEstado())
                .imagen(imagen)
                .build();
    }
    
    public void updateEntity(ProcesoElectoralUpdateRequestDTO requestDTO, 
                            ProcesoElectoral proceso, 
                            Imagen imagen) {
        if (requestDTO == null || proceso == null) return;
        
        if (requestDTO.getNombre() != null) {
            proceso.setNombre(requestDTO.getNombre());
        }
        
        if (requestDTO.getDescripcion() != null) {
            proceso.setDescripcion(requestDTO.getDescripcion());
        }
        
        if (requestDTO.getFechaInicio() != null) {
            proceso.setFechaInicio(requestDTO.getFechaInicio());
        }
        
        if (requestDTO.getFechaFin() != null) {
            proceso.setFechaFin(requestDTO.getFechaFin());
        }
        
        if (requestDTO.getEstado() != null) {
            proceso.setEstado(requestDTO.getEstado());
        }
        
        if (requestDTO.getImagenId() != null && imagen != null) {
            proceso.setImagen(imagen);
        }
    }
    
    private String determinarEstadoDescripcion(ProcesoElectoral proceso, LocalDate hoy) {
        if (!proceso.getEstado()) {
            return "INACTIVO";
        }
        
        if (hoy.isBefore(proceso.getFechaInicio())) {
            return "PROXIMO";
        } else if (hoy.isAfter(proceso.getFechaFin())) {
            return "FINALIZADO";
        } else {
            return "EN_CURSO";
        }
    }
    
    private List<CargoProcesoResumenDTO> convertirCargosProceso(List<CargoProceso> cargosProceso) {
        return cargosProceso.stream()
                .map(this::toCargoProcesoResumenDTO)
                .collect(Collectors.toList());
    }
    
    private CargoProcesoResumenDTO toCargoProcesoResumenDTO(CargoProceso cargoProceso) {
        if (cargoProceso == null) return null;
        
        return CargoProcesoResumenDTO.builder()
                .id(cargoProceso.getId())
                .nombre(cargoProceso.getNombre())
                .cargoId(cargoProceso.getCargo() != null ? cargoProceso.getCargo().getId() : null)
                .unidadNombre(cargoProceso.getCargo() != null && 
                             cargoProceso.getCargo().getUnidad() != null ? 
                             cargoProceso.getCargo().getUnidad().getNombre() : null)
                .totalCandidatos(cargoProceso.getCandidatos() != null ? 
                                 cargoProceso.getCandidatos().size() : 0)
                .activo(cargoProceso.getActivo())
                .build();
    }
    
    public List<ProcesoElectoralDTO> toDTOList(List<ProcesoElectoral> procesos) {
        return procesos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProcesoElectoralResponseDTO> toResponseDTOList(List<ProcesoElectoral> procesos) {
        return procesos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}