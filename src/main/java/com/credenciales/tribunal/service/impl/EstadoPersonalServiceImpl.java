package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.estadoActual.CambioEstadoMasivoRequestDTO;
import com.credenciales.tribunal.dto.estadoActual.EstadoActualDTO;
import com.credenciales.tribunal.dto.estadoActual.ResultadoCambioMasivoDTO;
import com.credenciales.tribunal.dto.personal.PersonalDTO;
import com.credenciales.tribunal.dto.estadoActual.CambioEstadoResquestDTO;
import com.credenciales.tribunal.model.entity.Estado;
import com.credenciales.tribunal.model.entity.EstadoActual;
import com.credenciales.tribunal.model.entity.Personal;
import com.credenciales.tribunal.model.enums.EstadoPersonal;
import com.credenciales.tribunal.repository.EstadoActualRepository;
import com.credenciales.tribunal.repository.EstadoRepository;
import com.credenciales.tribunal.repository.PersonalRepository;
import com.credenciales.tribunal.service.EstadoPersonalService;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EstadoPersonalServiceImpl implements EstadoPersonalService {

    private final PersonalRepository personalRepository;
    private final EstadoRepository estadoRepository;
    private final EstadoActualRepository estadoActualRepository;

    @Override
    public PersonalDTO registrarPersonal(CambioEstadoResquestDTO request) {
        Personal personal = personalRepository.findById(request.getPersonalId())
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado"));

        validarTransicionEstado(personal.getId(), EstadoPersonal.PERSONAL_REGISTRADO);

        Estado estadoRegistrado = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_REGISTRADO)
                .orElseThrow(() -> new BusinessException("Estado PERSONAL REGISTRADO no configurado"));

        // Desactivar estado actual si existe
        desactivarEstadoActual(personal.getId());

        // Crear nuevo estado
        EstadoActual nuevoEstado = EstadoActual.builder()
                .personal(personal)
                .estado(estadoRegistrado)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(nuevoEstado);
        log.info("Personal {} registrado exitosamente", personal.getId());

        return mapToDTO(personal);
    }

    @Override
    public PersonalDTO imprimirCredencial(Long personalId) {
        Personal personal = validarPersonal(personalId);

        // Verificar que el estado actual sea PERSONAL_REGISTRADO
        if (!estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                personalId, EstadoPersonal.PERSONAL_REGISTRADO.getNombre())) {
            throw new BusinessException("El personal debe estar en estado REGISTRADO para imprimir credencial");
        }

        Estado estadoImpreso = estadoRepository.findByEnum(EstadoPersonal.CREDENCIAL_IMPRESO)
                .orElseThrow(() -> new BusinessException("Estado CREDENCIAL IMPRESO no configurado"));

        desactivarEstadoActual(personalId);

        EstadoActual nuevoEstado = EstadoActual.builder()
                .personal(personal)
                .estado(estadoImpreso)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(nuevoEstado);
        log.info("Credencial impresa para personal {}", personalId);

        return mapToDTO(personal);
    }

    @Override
    public PersonalDTO entregarCredencial(Long personalId) {
        Personal personal = validarPersonal(personalId);

        // Verificar que el estado actual sea CREDENCIAL_IMPRESO
        if (!estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                personalId, EstadoPersonal.CREDENCIAL_IMPRESO.getNombre())) {
            throw new BusinessException("La credencial debe estar impresa antes de entregarla");
        }

        Estado estadoEntregado = estadoRepository.findByEnum(EstadoPersonal.CREDENCIAL_ENTREGADO)
                .orElseThrow(() -> new BusinessException("Estado CREDENCIAL ENTREGADO no configurado"));

        desactivarEstadoActual(personalId);

        // Crear estado CREDENCIAL ENTREGADO
        EstadoActual nuevoEstado = EstadoActual.builder()
                .personal(personal)
                .estado(estadoEntregado)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(nuevoEstado);

        // Automáticamente pasar a PERSONAL ACTIVO
        Estado estadoActivo = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_ACTIVO)
                .orElseThrow(() -> new BusinessException("Estado PERSONAL ACTIVO no configurado"));

        desactivarEstadoActual(personalId); // Desactivar CREDENCIAL ENTREGADO

        EstadoActual estadoActivoNuevo = EstadoActual.builder()
                .personal(personal)
                .estado(estadoActivo)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(estadoActivoNuevo);
        log.info("Credencial entregada y personal {} activado", personalId);

        return mapToDTO(personal);
    }

    @Override
    public PersonalDTO habilitarAccesoComputo(Long personalId) {
        Personal personal = validarPersonal(personalId);

        // Verificar que el personal tenga acceso a cómputo habilitado
        if (!personal.getAccesoComputo()) {
            throw new BusinessException("Este personal no tiene habilitado el acceso a cómputo");
        }

        // Verificar que esté en estado PERSONAL ACTIVO
        if (!estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                personalId, EstadoPersonal.PERSONAL_ACTIVO.getNombre())) {
            throw new BusinessException("El personal debe estar ACTIVO para habilitar acceso a cómputo");
        }

        Estado estadoComputo = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO)
                .orElseThrow(() -> new BusinessException("Estado PERSONAL CON ACCESO A COMPUTO no configurado"));

        desactivarEstadoActual(personalId);

        EstadoActual nuevoEstado = EstadoActual.builder()
                .personal(personal)
                .estado(estadoComputo)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(nuevoEstado);
        log.info("Acceso a cómputo habilitado para personal {}", personalId);

        return mapToDTO(personal);
    }

    @Override
    public PersonalDTO devolverCredencial(Long personalId) {
        Personal personal = validarPersonal(personalId);

        // Puede devolver credencial desde PERSONAL ACTIVO o PERSONAL CON ACCESO A
        // COMPUTO
        boolean esActivo = estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                personalId, EstadoPersonal.PERSONAL_ACTIVO.getNombre());
        boolean esAccesoComputo = estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                personalId, EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO.getNombre());

        if (!esActivo && !esAccesoComputo) {
            throw new BusinessException(
                    "El personal debe estar ACTIVO o con ACCESO A COMPUTO para devolver credencial");
        }

        Estado estadoDevuelto = estadoRepository.findByEnum(EstadoPersonal.CREDENCIAL_DEVUELTO)
                .orElseThrow(() -> new BusinessException("Estado CREDENCIAL DEVUELTO no configurado"));

        desactivarEstadoActual(personalId);

        EstadoActual nuevoEstado = EstadoActual.builder()
                .personal(personal)
                .estado(estadoDevuelto)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(nuevoEstado);

        // Al devolver credencial, pasar a INACTIVO
        return finalizarProcesoElectoral(personalId);
    }

    @Override
    public PersonalDTO finalizarProcesoElectoral(Long personalId) {
        Personal personal = validarPersonal(personalId);

        Estado estadoInactivoTerminado = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO)
                .orElseThrow(() -> new BusinessException("Estado INACTIVO PROCESO TERMINADO no configurado"));

        desactivarEstadoActual(personalId);

        EstadoActual nuevoEstado = EstadoActual.builder()
                .personal(personal)
                .estado(estadoInactivoTerminado)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(nuevoEstado);
        log.info("Proceso electoral finalizado para personal {}", personalId);

        return mapToDTO(personal);
    }

    @Override
    public PersonalDTO renunciar(Long personalId) {
        Personal personal = validarPersonal(personalId);

        // Validar que pueda renunciar desde ciertos estados
        boolean puedeRenunciar = estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                personalId, EstadoPersonal.PERSONAL_REGISTRADO.getNombre()) ||
                estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                        personalId, EstadoPersonal.CREDENCIAL_IMPRESO.getNombre())
                ||
                estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                        personalId, EstadoPersonal.CREDENCIAL_ENTREGADO.getNombre())
                ||
                estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                        personalId, EstadoPersonal.PERSONAL_ACTIVO.getNombre())
                ||
                estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                        personalId, EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO.getNombre());

        if (!puedeRenunciar) {
            throw new BusinessException("No se puede renunciar desde el estado actual");
        }

        Estado estadoRenuncia = estadoRepository.findByEnum(EstadoPersonal.INACTIVO_POR_RENUNCIA)
                .orElseThrow(() -> new BusinessException("Estado INACTIVO POR RENUNCIA no configurado"));

        desactivarEstadoActual(personalId);

        EstadoActual nuevoEstado = EstadoActual.builder()
                .personal(personal)
                .estado(estadoRenuncia)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(nuevoEstado);
        log.info("Renuncia registrada para personal {}", personalId);

        return mapToDTO(personal);
    }

    @Override
    public boolean validarTransicionEstado(Long personalId, EstadoPersonal nuevoEstado) {
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado"));

        EstadoActual estadoActual = estadoActualRepository.findCurrentEstadoByPersonalId(personalId)
                .orElse(null);

        if (estadoActual == null) {
            // Si no tiene estado, solo puede ser REGISTRADO
            return nuevoEstado == EstadoPersonal.PERSONAL_REGISTRADO;
        }

        String estadoActualNombre = estadoActual.getEstado().getNombre();

        // Lógica de transiciones permitidas
        switch (estadoActualNombre) {
            case "PERSONAL REGISTRADO":
                return nuevoEstado == EstadoPersonal.CREDENCIAL_IMPRESO ||
                        nuevoEstado == EstadoPersonal.INACTIVO_POR_RENUNCIA;

            case "CREDENCIAL IMPRESO":
                return nuevoEstado == EstadoPersonal.CREDENCIAL_ENTREGADO ||
                        nuevoEstado == EstadoPersonal.INACTIVO_POR_RENUNCIA;

            case "CREDENCIAL ENTREGADO":
                return nuevoEstado == EstadoPersonal.PERSONAL_ACTIVO; // Automático

            case "PERSONAL ACTIVO":
                return nuevoEstado == EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO ||
                        nuevoEstado == EstadoPersonal.CREDENCIAL_DEVUELTO ||
                        nuevoEstado == EstadoPersonal.INACTIVO_POR_RENUNCIA;

            case "PERSONAL CON ACCESO A COMPUTO":
                return nuevoEstado == EstadoPersonal.CREDENCIAL_DEVUELTO ||
                        nuevoEstado == EstadoPersonal.INACTIVO_POR_RENUNCIA;

            case "CREDENCIAL DEVUELTO":
                return nuevoEstado == EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO;

            default:
                return false;
        }
    }

    @Override
    public List<EstadoPersonal> obtenerEstadosPermitidos(Long personalId) {
        return Arrays.stream(EstadoPersonal.values())
                .filter(estado -> validarTransicionEstado(personalId, estado))
                .collect(Collectors.toList());
    }

    @Override
    public PersonalDTO obtenerPersonalConEstadoActual(Long personalId) {
        Personal personal = validarPersonal(personalId);
        return mapToDTO(personal);
    }

    @Override
    public List<PersonalDTO> listarPersonalPorEstado(EstadoPersonal estado) {
        List<Personal> personalList = personalRepository.findAllByCurrentEstado(estado.getNombre());
        return personalList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EstadoActualDTO> obtenerHistorialEstados(Long personalId) {
        return estadoActualRepository.findHistorialByPersonalId(personalId)
                .stream()
                .map(this::mapToEstadoActualDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean puedeHabilitarseAccesoComputo(Long personalId) {
        Personal personal = validarPersonal(personalId);
        return personal.getAccesoComputo() &&
                estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                        personalId, EstadoPersonal.PERSONAL_ACTIVO.getNombre());
    }

    @Override
    public ResultadoCambioMasivoDTO imprimirCredencialMasivo(CambioEstadoMasivoRequestDTO request) {
        ResultadoCambioMasivoDTO resultado = new ResultadoCambioMasivoDTO();
        List<PersonalDTO> actualizados = new ArrayList<>();
        Map<Long, String> errores = new HashMap<>();
        List<Long> exitosos = new ArrayList<>();

        resultado.setTotalProcesados(request.getPersonalIds().size());

        for (Long personalId : request.getPersonalIds()) {
            try {
                PersonalDTO personalActualizado = imprimirCredencial(personalId);
                actualizados.add(personalActualizado);
                exitosos.add(personalId);
            } catch (Exception e) {
                errores.put(personalId, e.getMessage());
            }
        }

        resultado.setExitosos(exitosos.size());
        resultado.setFallidos(errores.size());
        resultado.setIdsExitosos(exitosos);
        resultado.setErrores(errores);
        resultado.setPersonalesActualizados(actualizados);

        return resultado;
    }

    private Personal validarPersonal(Long personalId) {
        return personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + personalId));
    }

    private void desactivarEstadoActual(Long personalId) {
        estadoActualRepository.findCurrentEstadoByPersonalId(personalId)
                .ifPresent(estadoActual -> {
                    estadoActual.setValor_estado_actual(false);
                    estadoActualRepository.save(estadoActual);
                });
    }

    private PersonalDTO mapToDTO(Personal personal) {
        String estadoActual = estadoActualRepository.findCurrentEstadoByPersonalId(personal.getId())
                .map(ea -> ea.getEstado().getNombre())
                .orElse("SIN ESTADO");

        return PersonalDTO.builder()
                .id(personal.getId())
                .nombre(personal.getNombre())
                .apellidoPaterno(personal.getApellidoPaterno())
                .apellidoMaterno(personal.getApellidoMaterno())
                .carnetIdentidad(personal.getCarnetIdentidad())
                .correo(personal.getCorreo())
                .celular(personal.getCelular())
                .accesoComputo(personal.getAccesoComputo())
                .nroCircunscripcion(personal.getNroCircunscripcion())
                .tipo(personal.getTipo())
                .imagenId(personal.getImagen() != null ? personal.getImagen().getIdImagen() : null)
                .qrId(personal.getQr() != null ? personal.getQr().getId() : null)
                .estadoActual(estadoActual)
                .build();
    }

    @Override
    public PersonalDTO estadoRegistrado(Long personalId) {
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado"));

        boolean pudeVolverARegistrarse = estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                personalId, EstadoPersonal.CREDENCIAL_IMPRESO.getNombre());

        if (!pudeVolverARegistrarse) {
            throw new BusinessException("No puede volver a imprimir tiene que devolver el credencial.");
        }

        Estado estado = estadoRepository.findByEnum(EstadoPersonal.INACTIVO_POR_RENUNCIA)
                .orElseThrow(() -> new BusinessException("Estado INACTIVO POR RENUNCIA no configurado"));

        desactivarEstadoActual(personalId);

        EstadoActual nuevoEstado = EstadoActual.builder()
                .personal(personal)
                .estado(estado)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(nuevoEstado);
        log.info("Se habilito para volver a imprimir el credencial. a personal con id: {}", personalId);

        return mapToDTO(personal);
    }

    private EstadoActualDTO mapToEstadoActualDTO(EstadoActual estadoActual) {
        return EstadoActualDTO.builder()
                .id(estadoActual.getId())
                .personalId(estadoActual.getPersonal().getId())
                .personalNombre(estadoActual.getPersonal().getNombre() + " " +
                        estadoActual.getPersonal().getApellidoPaterno())
                .estadoNombre(estadoActual.getEstado().getNombre())
                .valorEstadoActual(estadoActual.getValor_estado_actual())
                .createdAt(estadoActual.getCreatedAt())
                .build();
    }
}
