package com.credenciales.tribunal.dto.historialcargoproceso;

import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoCreateRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoUpdateRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoResponseDTO;
import com.credenciales.tribunal.model.entity.CargoProceso;
import com.credenciales.tribunal.model.entity.HistorialCargoProceso;
import com.credenciales.tribunal.model.entity.Personal;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HistorialCargoProcesoMapper {
    
    public HistorialCargoProcesoDTO toDTO(HistorialCargoProceso historial) {
        if (historial == null) return null;
        
        Integer duracionDias = calcularDuracionDias(historial.getFechaInicio(), historial.getFechaFin());
        
        return HistorialCargoProcesoDTO.builder()
                .id(historial.getId())
                .cargoProcesoId(historial.getCargoProceso() != null ? historial.getCargoProceso().getId() : null)
                .cargoProcesoNombre(historial.getCargoProceso() != null ? historial.getCargoProceso().getNombre() : null)
                .procesoId(historial.getCargoProceso() != null && historial.getCargoProceso().getProceso() != null ? 
                        historial.getCargoProceso().getProceso().getId() : null)
                .procesoNombre(historial.getCargoProceso() != null && historial.getCargoProceso().getProceso() != null ? 
                        historial.getCargoProceso().getProceso().getNombre() : null)
                .unidadId(historial.getCargoProceso() != null && historial.getCargoProceso().getUnidad() != null ? 
                        historial.getCargoProceso().getUnidad().getId() : null)
                .unidadNombre(historial.getCargoProceso() != null && historial.getCargoProceso().getUnidad() != null ? 
                        historial.getCargoProceso().getUnidad().getNombre() : null)
                .personalId(historial.getPersonal() != null ? historial.getPersonal().getId() : null)
                .personalNombre(historial.getPersonal() != null ? historial.getPersonal().getNombre() : null)
                .personalApellido(historial.getPersonal() != null ? historial.getPersonal().getApellidoPaterno() : null)
                .personalApellido(historial.getPersonal() != null ? historial.getPersonal().getApellidoMaterno() : null)
                .personalDocumento(historial.getPersonal() != null ? historial.getPersonal().getCarnetIdentidad() : null)
                .fechaInicio(historial.getFechaInicio())
                .fechaFin(historial.getFechaFin())
                .activo(historial.getActivo())
                .duracionDias(duracionDias)
                .build();
    }
    
    public HistorialCargoProcesoResponseDTO toResponseDTO(HistorialCargoProceso historial) {
        if (historial == null) return null;
        
        Long duracionHoras = calcularDuracionHoras(historial.getFechaInicio(), historial.getFechaFin());
        Long duracionDias = calcularDuracionDiasLong(historial.getFechaInicio(), historial.getFechaFin());
        String estado = determinarEstado(historial);
        
        return HistorialCargoProcesoResponseDTO.builder()
                .id(historial.getId())
                
                // Cargo proceso
                .cargoProcesoId(historial.getCargoProceso() != null ? historial.getCargoProceso().getId() : null)
                .cargoProcesoNombre(historial.getCargoProceso() != null ? historial.getCargoProceso().getNombre() : null)
                .cargoProcesoDescripcion(historial.getCargoProceso() != null ? historial.getCargoProceso().getDescripcion() : null)
                //.cargoProcesoActivo(historial.getCargoProceso() != null ? historial.getCargoProceso().getActivo() : null)
                
                // Proceso
                .procesoId(historial.getCargoProceso() != null && historial.getCargoProceso().getProceso() != null ? 
                        historial.getCargoProceso().getProceso().getId() : null)
                .procesoNombre(historial.getCargoProceso() != null && historial.getCargoProceso().getProceso() != null ? 
                        historial.getCargoProceso().getProceso().getNombre() : null)
                .procesoFechaInicio(historial.getCargoProceso() != null && historial.getCargoProceso().getProceso() != null && 
                        historial.getCargoProceso().getProceso().getFechaInicio() != null ? 
                        historial.getCargoProceso().getProceso().getFechaInicio().atStartOfDay() : null)
                .procesoFechaFin(historial.getCargoProceso() != null && historial.getCargoProceso().getProceso() != null && 
                        historial.getCargoProceso().getProceso().getFechaFin() != null ? 
                        historial.getCargoProceso().getProceso().getFechaFin().atStartOfDay() : null)
                .procesoActivo(historial.getCargoProceso() != null && historial.getCargoProceso().getProceso() != null ? 
                        historial.getCargoProceso().getProceso().getEstado() : null)
                
                // Unidad
                .unidadId(historial.getCargoProceso() != null && historial.getCargoProceso().getUnidad() != null ? 
                        historial.getCargoProceso().getUnidad().getId() : null)
                .unidadNombre(historial.getCargoProceso() != null && historial.getCargoProceso().getUnidad() != null ? 
                        historial.getCargoProceso().getUnidad().getNombre() : null)
                .unidadAbreviatura(historial.getCargoProceso() != null && historial.getCargoProceso().getUnidad() != null ? 
                        historial.getCargoProceso().getUnidad().getAbreviatura() : null)
                
                // Personal
                .personalId(historial.getPersonal() != null ? historial.getPersonal().getId() : null)
                .personalNombre(historial.getPersonal() != null ? historial.getPersonal().getNombre() : null)
                .personalApellido(historial.getPersonal() != null ? historial.getPersonal().getApellidoPaterno() : null)
                .personalApellido(historial.getPersonal() != null ? historial.getPersonal().getApellidoMaterno() : null)
                .personalDocumento(historial.getPersonal() != null ? historial.getPersonal().getCarnetIdentidad() : null)
                .personalEmail(historial.getPersonal() != null ? historial.getPersonal().getCorreo() : null)
                
                // Historial
                .fechaInicio(historial.getFechaInicio())
                .fechaFin(historial.getFechaFin())
                .activo(historial.getActivo())
                .duracionHoras(duracionHoras)
                .duracionDias(duracionDias)
                .estado(estado)
                .build();
    }
    
    public HistorialCargoProceso toEntity(HistorialCargoProcesoCreateRequestDTO requestDTO, 
                                         CargoProceso cargoProceso, 
                                         Personal personal) {
        if (requestDTO == null) return null;
        
        return HistorialCargoProceso.builder()
                .cargoProceso(cargoProceso)
                .personal(personal)
                .fechaInicio(requestDTO.getFechaInicio())
                .fechaFin(requestDTO.getFechaFin())
                .activo(requestDTO.getActivo())
                .build();
    }
    
    public void updateEntity(HistorialCargoProcesoUpdateRequestDTO requestDTO, 
                            HistorialCargoProceso historial) {
        if (requestDTO == null || historial == null) return;
        
        if (requestDTO.getFechaInicio() != null) {
            historial.setFechaInicio(requestDTO.getFechaInicio());
        }
        
        if (requestDTO.getFechaFin() != null) {
            historial.setFechaFin(requestDTO.getFechaFin());
        }
        
        if (requestDTO.getActivo() != null) {
            historial.setActivo(requestDTO.getActivo());
            // Si se desactiva y no tiene fecha fin, establecer fecha fin actual
            if (!requestDTO.getActivo() && historial.getFechaFin() == null) {
                historial.setFechaFin(LocalDateTime.now());
            }
        }
    }
    
    private Integer calcularDuracionDias(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null) return null;
        LocalDateTime fechaFin = fin != null ? fin : LocalDateTime.now();
        return (int) ChronoUnit.DAYS.between(inicio, fechaFin);
    }
    
    private Long calcularDuracionDiasLong(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null) return null;
        LocalDateTime fechaFin = fin != null ? fin : LocalDateTime.now();
        return ChronoUnit.DAYS.between(inicio, fechaFin);
    }
    
    private Long calcularDuracionHoras(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null) return null;
        LocalDateTime fechaFin = fin != null ? fin : LocalDateTime.now();
        return Duration.between(inicio, fechaFin).toHours();
    }
    
    private String determinarEstado(HistorialCargoProceso historial) {
        if (!historial.getActivo()) {
            return "FINALIZADO";
        }
        if (historial.getFechaFin() != null && historial.getFechaFin().isBefore(LocalDateTime.now())) {
            return "FINALIZADO";
        }
        return "ACTIVO";
    }
    
    public List<HistorialCargoProcesoDTO> toDTOList(List<HistorialCargoProceso> historiales) {
        return historiales.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<HistorialCargoProcesoResponseDTO> toResponseDTOList(List<HistorialCargoProceso> historiales) {
        return historiales.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}