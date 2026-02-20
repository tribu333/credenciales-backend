package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.externo.ExternoDTO;
import com.credenciales.tribunal.dto.externo.ExternoDetalleResponseDTO;
import com.credenciales.tribunal.dto.externo.ExternoMapper;
import com.credenciales.tribunal.dto.externo.ExternoRequestDTO;
import com.credenciales.tribunal.dto.externo.ExternoResponseDTO;
import com.credenciales.tribunal.exception.DuplicateResourceException;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.model.entity.Externo;
import com.credenciales.tribunal.model.entity.Imagen;
import com.credenciales.tribunal.model.enums.TipoExterno;
import com.credenciales.tribunal.repository.ExternoRepository;
import com.credenciales.tribunal.repository.ImagenRepository;
import com.credenciales.tribunal.service.ExternoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExternoServiceImpl implements ExternoService {

    private final ExternoRepository externoRepository;
    private final ImagenRepository imagenRepository;
    private final ExternoMapper externoMapper;

    private static final String EXTERNO_NO_ENCONTRADO = "Externo no encontrado con ID: ";

    @Override
    public ExternoResponseDTO crearExterno(ExternoRequestDTO requestDTO) {
        log.info("Creando nuevo externo: {}", requestDTO.getNombreCompleto());
        
        // Validar duplicados
        validarCamposUnicos(requestDTO, null);
        
        // Obtener imagen si se proporcionó
        Imagen imagen = obtenerImagenSiExiste(requestDTO.getImagenId());
        
        // Crear entidad
        Externo externo = externoMapper.toEntity(requestDTO, imagen);
        
        // Guardar
        Externo savedExterno = externoRepository.save(externo);
        log.info("Externo creado exitosamente con ID: {}", savedExterno.getId());
        
        return externoMapper.toResponseDTO(savedExterno);
    }

    @Override
    public ExternoResponseDTO actualizarExterno(Long id, ExternoRequestDTO requestDTO) {
        log.info("Actualizando externo con ID: {}", id);
        
        // Buscar externo existente
        Externo externo = externoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EXTERNO_NO_ENCONTRADO + id));
        
        // Validar campos únicos (excluyendo el actual)
        validarCamposUnicos(requestDTO, id);
        
        // Obtener nueva imagen si se proporcionó
        Imagen imagen = obtenerImagenSiExiste(requestDTO.getImagenId());
        
        // Actualizar entidad
        externoMapper.updateEntity(requestDTO, externo, imagen);
        
        // Guardar cambios
        Externo updatedExterno = externoRepository.save(externo);
        log.info("Externo actualizado exitosamente con ID: {}", updatedExterno.getId());
        
        return externoMapper.toResponseDTO(updatedExterno);
    }

    @Override
    public void eliminarExterno(Long id) {
        log.info("Eliminando externo con ID: {}", id);
        
        if (!externoRepository.existsById(id)) {
            throw new ResourceNotFoundException(EXTERNO_NO_ENCONTRADO + id);
        }
        
        externoRepository.deleteById(id);
        log.info("Externo eliminado exitosamente con ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ExternoDTO obtenerExternoPorId(Long id) {
        log.debug("Buscando externo por ID: {}", id);
        
        Externo externo = externoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EXTERNO_NO_ENCONTRADO + id));
        
        return externoMapper.toDTO(externo);
    }

    @Override
    @Transactional(readOnly = true)
    public ExternoResponseDTO obtenerExternoResponsePorId(Long id) {
        log.debug("Buscando externo response por ID: {}", id);
        
        Externo externo = externoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(EXTERNO_NO_ENCONTRADO + id));
        
        return externoMapper.toResponseDTO(externo);
    }

    @Override
    @Transactional(readOnly = true)
    public ExternoDetalleResponseDTO obtenerExternoDetallePorId(Long id) {
        log.debug("Buscando detalle de externo por ID: {}", id);
        
        Externo externo = externoRepository.findByIdWithImagenAndAsignaciones(id);
        if (externo == null) {
            throw new ResourceNotFoundException(EXTERNO_NO_ENCONTRADO + id);
        }
        
        return externoMapper.toDetalleResponseDTO(externo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternoDTO> listarTodos() {
        log.debug("Listando todos los externos (DTO básico)");
        return externoMapper.toDTOList(externoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternoResponseDTO> listarTodosResponse() {
        log.debug("Listando todos los externos (Response DTO)");
        return externoMapper.toResponseDTOList(externoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternoDetalleResponseDTO> listarTodosDetalles() {
        log.debug("Listando todos los externos con detalles completos");
        return externoMapper.toDetalleResponseDTOList(externoRepository.findAllWithImagenAndAsignaciones());
    }

    @Override
    @Transactional(readOnly = true)
    public ExternoDTO obtenerPorCarnetIdentidad(String carnetIdentidad) {
        log.debug("Buscando externo por carnet de identidad: {}", carnetIdentidad);
        
        Externo externo = externoRepository.findByCarnetIdentidad(carnetIdentidad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Externo no encontrado con carnet de identidad: " + carnetIdentidad));
        
        return externoMapper.toDTO(externo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternoDTO> listarPorIdentificador(String identificador) {
        log.debug("Listando externos por identificador exacto: {}", identificador);
        
        List<Externo> externos = externoRepository.findByIdentificador(identificador);
        
        if (externos.isEmpty()) {
            log.warn("No se encontraron externos con el identificador: {}", identificador);
        }
        
        return externoMapper.toDTOList(externos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternoDTO> listarPorIdentificadorParcial(String identificador) {
        log.debug("Listando externos por identificador parcial: {}", identificador);
        
        List<Externo> externos = externoRepository.findByIdentificadorContainingIgnoreCase(identificador);
        
        if (externos.isEmpty()) {
            log.warn("No se encontraron externos con identificador que contenga: {}", identificador);
        }
        
        return externoMapper.toDTOList(externos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternoDTO> listarPorTipoExterno(TipoExterno tipoExterno) {
        log.debug("Listando externos por tipo: {}", tipoExterno);
        return externoMapper.toDTOList(externoRepository.findByTipoExterno(tipoExterno));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternoDTO> listarPorOrganizacionPolitica(String orgPolitica) {
        log.debug("Listando externos por organización política: {}", orgPolitica);
        return externoMapper.toDTOList(externoRepository.findByOrgPoliticaContainingIgnoreCase(orgPolitica));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCarnetIdentidad(String carnetIdentidad) {
        return externoRepository.findByCarnetIdentidad(carnetIdentidad).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeAlgunoPorIdentificador(String identificador) {
        return externoRepository.findByIdentificador(identificador).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarTotal() {
        return externoRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorTipoExterno(TipoExterno tipoExterno) {
        return externoRepository.findByTipoExterno(tipoExterno).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorIdentificador(String identificador) {
        return externoRepository.findByIdentificador(identificador).size();
    }
    // Métodos privados de ayuda
    private Imagen obtenerImagenSiExiste(Long imagenId) {
        if (imagenId == null) {
            return null;
        }
        
        return imagenRepository.findById(imagenId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + imagenId));
    }

    private void validarCamposUnicos(ExternoRequestDTO requestDTO, Long idExternoActual) {
        // Validar carnet de identidad único (solo si se proporciona)
        if (requestDTO.getCarnetIdentidad() != null && !requestDTO.getCarnetIdentidad().isEmpty()) {
            externoRepository.findByCarnetIdentidad(requestDTO.getCarnetIdentidad())
                    .ifPresent(externo -> {
                        if (idExternoActual == null || !externo.getId().equals(idExternoActual)) {
                            throw new DuplicateResourceException(
                                    "Ya existe un externo con el carnet de identidad: " + requestDTO.getCarnetIdentidad());
                        }
                    });
        }
        
        // Nota: identificador NO es único, puede haber múltiples personas
        // de la misma organización, por eso no se valida duplicado aquí
    }
}