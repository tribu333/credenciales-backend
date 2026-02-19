package com.credenciales.tribunal.dto.externo;

import com.credenciales.tribunal.dto.externo.AsignacionQrBasicaDTO;
import com.credenciales.tribunal.model.entity.AsignacionQr;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AsignacionQrMapper {
    
    public AsignacionQrBasicaDTO toBasicaDTO(AsignacionQr asignacion) {
        if (asignacion == null) return null;
        
        return AsignacionQrBasicaDTO.builder()
                .id(asignacion.getId())
                //.codigoQr(asignacion.getQr())
                .fechaAsignacion(asignacion.getFechaAsignacion())
                .activo(asignacion.getActivo())
                .build();
    }
    
    public List<AsignacionQrBasicaDTO> toBasicaDTOList(List<AsignacionQr> asignaciones) {
        if (asignaciones == null) return null;
        
        return asignaciones.stream()
                .map(this::toBasicaDTO)
                .collect(Collectors.toList());
    }
}