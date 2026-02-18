package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.estadoActual.EstadoActualDTO;
import com.credenciales.tribunal.dto.estadoActual.ResultadoCambioMasivoDTO;
import com.credenciales.tribunal.dto.personal.PersonalDTO;
import com.credenciales.tribunal.dto.estadoActual.CambioEstadoMasivoRequestDTO;
import com.credenciales.tribunal.dto.estadoActual.CambioEstadoResquestDTO;
import com.credenciales.tribunal.model.enums.EstadoPersonal;
import java.util.List;

public interface EstadoPersonalService {
    
    // Cambios de estado principales
    PersonalDTO registrarPersonal(CambioEstadoResquestDTO request);
    PersonalDTO imprimirCredencial(Long personalId);
    PersonalDTO entregarCredencial(Long personalId);
    PersonalDTO habilitarAccesoComputo(Long personalId);
    PersonalDTO devolverCredencial(Long personalId);
    PersonalDTO finalizarProcesoElectoral(Long personalId);
    PersonalDTO renunciar(Long personalId);
    
    // Validaciones
    boolean validarTransicionEstado(Long personalId, EstadoPersonal nuevoEstado);
    List<EstadoPersonal> obtenerEstadosPermitidos(Long personalId);
    
    // Consultas
    PersonalDTO obtenerPersonalConEstadoActual(Long personalId);
    List<PersonalDTO> listarPersonalPorEstado(EstadoPersonal estado);
    List<EstadoActualDTO> obtenerHistorialEstados(Long personalId);
    boolean puedeHabilitarseAccesoComputo(Long personalId);
    ResultadoCambioMasivoDTO imprimirCredencialMasivo(CambioEstadoMasivoRequestDTO request);
}
