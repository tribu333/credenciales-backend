package com.credenciales.tribunal.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.credenciales.tribunal.dto.contrato.ContratoCreateRequestDTO;
import com.credenciales.tribunal.dto.contrato.ContratoResponseDTO;
import com.credenciales.tribunal.dto.contrato.ContratoUpdateRequestDTO;
import com.credenciales.tribunal.dto.historialcargoproceso.HistorialCargoProcesoMapper;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.exception.DuplicateResourceException;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.model.entity.CargoProceso;
import com.credenciales.tribunal.model.entity.HistorialCargoProceso;
import com.credenciales.tribunal.model.entity.Personal;
import com.credenciales.tribunal.model.entity.ProcesoElectoral;
import com.credenciales.tribunal.repository.ContratoRepository;
import com.credenciales.tribunal.repository.PersonalRepository;
import com.credenciales.tribunal.repository.ProcesoElectoralRepository;
import com.credenciales.tribunal.service.ContratoService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContratoServiceImpl implements ContratoService{

    private final ContratoRepository contratoRepository;
    private final PersonalRepository personalRepository;
    private final ProcesoElectoralRepository procesoRepository;
    private final HistorialCargoProcesoMapper historialMapper;
    @Override
    public ContratoResponseDTO createContrato(ContratoCreateRequestDTO requestDTO) {
        log.info("Creando nuevo historial de cargo proceso para personal ID: {} y cargo proceso ID: {}", 
                requestDTO.getPersonalId(), requestDTO.getCargoProcesoId());
        
        // Validar que el cargo proceso exista y esté activo
        CargoProceso cargoProceso = cargoProcesoRepository.findById(requestDTO.getCargoProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo proceso no encontrado con ID: " + requestDTO.getCargoProcesoId()));
        
        // if (!cargoProceso.getActivo()) {
        //     throw new BusinessException("No se puede asignar personal a un cargo proceso inactivo");
        // }
        
        // Validar que el proceso del cargo esté activo y vigente
        ProcesoElectoral proceso = cargoProceso.getProceso();
        if (!proceso.getEstado()) {
            throw new BusinessException("No se puede asignar personal a un cargo de un proceso inactivo");
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isBefore(proceso.getFechaInicio().atStartOfDay()) || 
            ahora.isAfter(proceso.getFechaFin().atStartOfDay())) {
            throw new BusinessException("No se puede asignar personal a un cargo fuera del período del proceso electoral");
        }
        
        // Validar que el personal exista
        Personal personal = personalRepository.findById(requestDTO.getPersonalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personal no encontrado con ID: " + requestDTO.getPersonalId()));
        
        // Validar que el personal no tenga un historial activo en el mismo cargo proceso
        if (historialRepository.existsByPersonalIdAndCargoProcesoIdAndActivoTrue(
                requestDTO.getPersonalId(), requestDTO.getCargoProcesoId())) {
            throw new DuplicateResourceException(
                    String.format("El personal ya tiene un historial activo en el cargo '%s'", 
                            cargoProceso.getNombre()));
        }
        
        // Validar que el personal pueda ser asignado (no tenga otro historial activo en otro cargo del mismo proceso)
        if (!puedeAsignarPersonalACargoProceso(requestDTO.getPersonalId(), requestDTO.getCargoProcesoId())) {
            throw new BusinessException(
                    "El personal ya tiene un historial activo en otro cargo del mismo proceso");
        }
        
        // Validar fechas
        validarFechas(requestDTO.getFechaInicio(), requestDTO.getFechaFin(), proceso);
        
        // Si el historial se crea como inactivo, debe tener fecha de fin
        if (!requestDTO.getActivo() && requestDTO.getFechaFin() == null) {
            throw new BusinessException("Si el historial se crea como inactivo, debe tener fecha de fin");
        }
        
        HistorialCargoProceso historial = historialMapper.toEntity(requestDTO, cargoProceso, personal);
        HistorialCargoProceso savedHistorial = historialRepository.save(historial);
        log.info("Historial de cargo proceso creado exitosamente con ID: {}", savedHistorial.getId());
        
        return historialMapper.toResponseDTO(savedHistorial);
    }

    @Override
    public ContratoResponseDTO getContratoById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getContratoById'");
    }

    @Override
    public List<ContratoResponseDTO> getAllContratos() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllContratos'");
    }

    @Override
    public ContratoResponseDTO updateContrato(Long id, ContratoUpdateRequestDTO requestDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateContrato'");
    }
    
}
