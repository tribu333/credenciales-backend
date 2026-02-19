package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.externo.ExternoDTO;
import com.credenciales.tribunal.dto.externo.ExternoDetalleResponseDTO;
import com.credenciales.tribunal.dto.externo.ExternoRequestDTO;
import com.credenciales.tribunal.dto.externo.ExternoResponseDTO;
import com.credenciales.tribunal.model.enums.TipoExterno;

import java.util.List;

public interface ExternoService {
    
    // CRUD básico
    ExternoResponseDTO crearExterno(ExternoRequestDTO requestDTO);
    
    ExternoResponseDTO actualizarExterno(Long id, ExternoRequestDTO requestDTO);
    
    void eliminarExterno(Long id);
    
    // Getters básicos
    ExternoDTO obtenerExternoPorId(Long id);
    
    ExternoResponseDTO obtenerExternoResponsePorId(Long id);
    
    ExternoDetalleResponseDTO obtenerExternoDetallePorId(Long id);
    
    List<ExternoDTO> listarTodos();
    
    List<ExternoResponseDTO> listarTodosResponse();
    
    List<ExternoDetalleResponseDTO> listarTodosDetalles();
    
    // Búsquedas específicas
    ExternoDTO obtenerPorCarnetIdentidad(String carnetIdentidad);
    
    // Cambiado: ahora devuelve List<ExternoDTO>
    List<ExternoDTO> listarPorIdentificador(String identificador);
    
    // Nueva búsqueda: por identificador parcial
    List<ExternoDTO> listarPorIdentificadorParcial(String identificador);
    
    List<ExternoDTO> listarPorTipoExterno(TipoExterno tipoExterno);
    
    List<ExternoDTO> listarPorOrganizacionPolitica(String orgPolitica);
    
    // Métodos de verificación
    boolean existePorCarnetIdentidad(String carnetIdentidad);
    
    boolean existeAlgunoPorIdentificador(String identificador);
    
    // Métodos de conteo
    long contarTotal();
    
    long contarPorTipoExterno(TipoExterno tipoExterno);
    long contarPorIdentificador(String identificador);
}