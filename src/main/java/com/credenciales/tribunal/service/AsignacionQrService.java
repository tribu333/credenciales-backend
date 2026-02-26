package com.credenciales.tribunal.service;

import com.credenciales.tribunal.model.entity.AsignacionQr;

import java.util.List;
import java.util.Optional;

public interface AsignacionQrService {
    
    // Métodos CRUD básicos
    List<AsignacionQr> findAll();
    
    AsignacionQr findById(Long id);
    
    AsignacionQr save(AsignacionQr asignacionQr);
    
    AsignacionQr update(Long id, AsignacionQr asignacionQr);
    
    void deleteById(Long id);
    
    // Métodos de consulta específicos
    List<AsignacionQr> findByExternoId(Long externoId);
    
    List<AsignacionQr> findByExternoIdAndActivoTrue(Long externoId);
    
    Optional<AsignacionQr> findActivaByExternoId(Long externoId);
    
    List<AsignacionQr> findByActivoTrue();
    
    // Métodos de negocio
    AsignacionQr liberarAsignacion(Long id);
    
    boolean existsByExternoIdAndActivoTrue(Long externoId);
}