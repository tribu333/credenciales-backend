package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.asignacionesqr.AsignacionRequestDTO;
import com.credenciales.tribunal.dto.asignacionesqr.AsignacionResponseDTO;
import com.credenciales.tribunal.dto.asignacionesqr.AsignacionResponseDetalDTO;

import java.util.List;

public interface AsignacionQrService {
    
    // Métodos CRUD con DTOs
    List<AsignacionResponseDTO> findAll();
    
    AsignacionResponseDTO findById(Long id);
    
    AsignacionResponseDTO create(AsignacionRequestDTO requestDTO);
    
    AsignacionResponseDTO update(Long id, AsignacionRequestDTO requestDTO);
    
    void deleteById(Long id);
    
    // Métodos de consulta específicos con DTOs
    List<AsignacionResponseDTO> findByExternoId(Long externoId);
    
    List<AsignacionResponseDTO> findByExternoIdAndActivoTrue(Long externoId);
    
    AsignacionResponseDTO findActivaByExternoId(Long externoId);
    
    List<AsignacionResponseDTO> findByActivoTrue();
    
    // Métodos de negocio
    AsignacionResponseDTO liberarAsignacion(Long id);
    
    boolean existsByExternoIdAndActivoTrue(Long externoId);
    AsignacionResponseDetalDTO findByExternoCodQr(String codQr);
}