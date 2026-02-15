package com.credenciales.tribunal.dto.historialcargo;

//import com.credenciales.tribunal.dto.historialcargo.HistorialCargoDTO;
/* import com.credenciales.tribunal.dto.historialcargo.request.HistorialCargoCreateRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.request.HistorialCargoUpdateRequestDTO;
import com.credenciales.tribunal.dto.historialcargo.response.HistorialCargoResponseDTO; */
import com.credenciales.tribunal.model.entity.Cargo;
import com.credenciales.tribunal.model.entity.HistorialCargo;
import com.credenciales.tribunal.model.entity.Personal;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HistorialCargoMapper {
    
    public HistorialCargoDTO toDTO(HistorialCargo historial) {
        if (historial == null) return null;
        
        return HistorialCargoDTO.builder()
                .id(historial.getId())
                .personalId(historial.getPersonal() != null ? historial.getPersonal().getId() : null)
                .personalNombre(historial.getPersonal() != null ? historial.getPersonal().getNombre() : null)
                .personalApellido(historial.getPersonal() != null ? historial.getPersonal().getApellidoPaterno() : null)
                .personalApellido(historial.getPersonal() != null ? historial.getPersonal().getApellidoMaterno() : null)
                .cargoId(historial.getCargo() != null ? historial.getCargo().getId() : null)
                .cargoNombre(historial.getCargo() != null ? historial.getCargo().getNombre() : null)
                .unidadNombre(historial.getCargo() != null && historial.getCargo().getUnidad() != null ? 
                        historial.getCargo().getUnidad().getNombre() : null)
                .fechaInicio(historial.getFechaInicio())
                .fechaFin(historial.getFechaFin())
                .activo(historial.getActivo())
                .build();
    }
    
    public HistorialCargoResponseDTO toResponseDTO(HistorialCargo historial) {
        if (historial == null) return null;
        
        Integer duracionDias = null;
        if (historial.getFechaInicio() != null) {
            LocalDateTime fechaFin = historial.getFechaFin() != null ? 
                    historial.getFechaFin() : LocalDateTime.now();
            duracionDias = (int) ChronoUnit.DAYS.between(
                    historial.getFechaInicio(), fechaFin);
        }
        
        return HistorialCargoResponseDTO.builder()
                .id(historial.getId())
                .personalId(historial.getPersonal() != null ? historial.getPersonal().getId() : null)
                .personalNombre(historial.getPersonal() != null ? historial.getPersonal().getNombre() : null)
                .personalApellido(historial.getPersonal() != null ? historial.getPersonal().getApellidoPaterno() : null)
                .personalApellido(historial.getPersonal() != null ? historial.getPersonal().getApellidoMaterno() : null)
                .personalDocumento(historial.getPersonal() != null ? historial.getPersonal().getCarnetIdentidad() : null)
                .cargoId(historial.getCargo() != null ? historial.getCargo().getId() : null)
                .cargoNombre(historial.getCargo() != null ? historial.getCargo().getNombre() : null)
                .unidadId(historial.getCargo() != null && historial.getCargo().getUnidad() != null ? 
                        historial.getCargo().getUnidad().getId() : null)
                .unidadNombre(historial.getCargo() != null && historial.getCargo().getUnidad() != null ? 
                        historial.getCargo().getUnidad().getNombre() : null)
                .unidadAbreviatura(historial.getCargo() != null && historial.getCargo().getUnidad() != null ? 
                        historial.getCargo().getUnidad().getAbreviatura() : null)
                .fechaInicio(historial.getFechaInicio())
                .fechaFin(historial.getFechaFin())
                .activo(historial.getActivo())
                .duracionDias(duracionDias)
                .build();
    }
    
    public HistorialCargo toEntity(HistorialCargoCreateRequestDTO requestDTO, 
                                   Personal personal, Cargo cargo) {
        if (requestDTO == null) return null;
        
        return HistorialCargo.builder()
                .personal(personal)
                .cargo(cargo)
                .fechaInicio(requestDTO.getFechaInicio())
                .fechaFin(requestDTO.getFechaFin())
                .activo(requestDTO.getActivo())
                .build();
    }
    
    public void updateEntity(HistorialCargoUpdateRequestDTO requestDTO, 
                            HistorialCargo historial) {
        if (requestDTO == null || historial == null) return;
        
        if (requestDTO.getFechaInicio() != null) {
            historial.setFechaInicio(requestDTO.getFechaInicio());
        }
        
        if (requestDTO.getFechaFin() != null) {
            historial.setFechaFin(requestDTO.getFechaFin());
        }
        
        if (requestDTO.getActivo() != null) {
            historial.setActivo(requestDTO.getActivo());
            // Si se desactiva, establecer fecha fin si no tiene
            if (!requestDTO.getActivo() && historial.getFechaFin() == null) {
                historial.setFechaFin(LocalDateTime.now());
            }
        }
    }
    
    public List<HistorialCargoDTO> toDTOList(List<HistorialCargo> historiales) {
        return historiales.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<HistorialCargoResponseDTO> toResponseDTOList(List<HistorialCargo> historiales) {
        return historiales.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}