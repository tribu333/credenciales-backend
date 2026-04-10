package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.estadoActual.CambioEstadoMasivoRequestDTO;
import com.credenciales.tribunal.dto.estadoActual.EstadoActualDTO;
import com.credenciales.tribunal.dto.estadoActual.ResultadoCambioMasivoDTO;
import com.credenciales.tribunal.dto.personal.PersonalDTO;
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

    private Personal validarPersonal(Long personalId) {
        return personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + personalId));
    }

    /**
     * Método genérico para cambiar de estado de forma ATÓMICA.
     * Primero desactiva el estado actual y luego crea el nuevo en una sola operación de base de datos.
     * La anotación @Transactional garantiza que todo se haga en una sola transacción.
     */
    @Transactional
    protected PersonalDTO cambiarEstadoPersonal(Long personalId, EstadoPersonal nuevoEstadoEnum, String reglaValidacionMensaje) {
        Estado nuevoEstado = estadoRepository.findByEnum(nuevoEstadoEnum)
                .orElseThrow(() -> new BusinessException("Estado " + nuevoEstadoEnum.getNombre() + " no configurado en el sistema"));

        Personal personal = personalRepository.findPersonalByIdWithPessimisticLock(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + personalId));

        if (!validarTransicionEstado(personalId, nuevoEstadoEnum)) {
            throw new BusinessException(reglaValidacionMensaje);
        }

        estadoActualRepository.findCurrentEstadoByPersonalId(personalId)
                .ifPresent(estadoActual -> {
                    estadoActual.setValor_estado_actual(false);
                    estadoActualRepository.save(estadoActual);
                });

        EstadoActual nuevoEstadoActual = EstadoActual.builder()
                .personal(personal)
                .estado(nuevoEstado)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(nuevoEstadoActual);
        log.info("Personal ID {} cambió a estado {}", personalId, nuevoEstadoEnum.getNombre());

        return mapToDTO(personal);
    }

    @Override
    public PersonalDTO imprimirCredencial(Long personalId) {
        return cambiarEstadoPersonal(
                personalId,
                EstadoPersonal.CREDENCIAL_IMPRESO,
                "El personal debe estar en estado REGISTRADO para imprimir credencial"
        );
    }

    @Override
    public PersonalDTO entregarCredencial(Long personalId) {
        PersonalDTO personal = cambiarEstadoPersonal(personalId,
                EstadoPersonal.CREDENCIAL_ENTREGADO,
                "La credencial debe estar impresa antes de entregarla");

        return cambiarEstadoPersonal(personalId, EstadoPersonal.PERSONAL_ACTIVO, "La credencial debe ser entregada al personal para que este activa");
    }

    @Override
    public PersonalDTO habilitarAccesoComputo(Long personalId) {
        Personal personal = validarPersonal(personalId);

        // if (Boolean.FALSE.equals(personal.getAccesoComputo())) {
        //     throw new BusinessException("El personal no tiene habilitado el acceso a cómputo");
        // }

        boolean yaTieneAcceso = estadoActualRepository
                .existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                        personalId, EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO.getNombre());

        if (yaTieneAcceso) {
            throw new BusinessException("El personal ya tiene habilitado el acceso a cómputo");
        }

        return cambiarEstadoPersonal(
                personalId,
                EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO,
                "El personal debe estar ACTIVO para habilitar acceso a cómputo"
        );
    }

    @Override
    public PersonalDTO devolverCredencial(Long personalId) {
        return cambiarEstadoPersonal(personalId,
                EstadoPersonal.CREDENCIAL_DEVUELTO,
                "El personal debe estar ACTIVO o con ACCESO A COMPUTO para devolver credencial");
    }

    @Override
    public PersonalDTO finalizarProcesoElectoral(Long personalId) {
        return cambiarEstadoPersonal(
                personalId,
                EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO,
                "No se puede finalizar el proceso desde el estado actual"
        );
    }

    @Override
    public PersonalDTO contratoTerminado(Long personalId) {
        return cambiarEstadoPersonal(
                personalId,
                EstadoPersonal.INACTIVO_CONTRATO_TERMINADO,
                "No se puede finalizar el CONTRATO desde el estado actual"
        );
    }

    @Override
    public PersonalDTO renunciar(Long personalId) {
        return cambiarEstadoPersonal(
                personalId,
                EstadoPersonal.INACTIVO_POR_RENUNCIA,
                "No se puede renunciar desde el estado actual"
        );
    }

    @Override
    public PersonalDTO estadoRegistrado(Long personalId) {
        return cambiarEstadoPersonal(
                personalId,
                EstadoPersonal.PERSONAL_REGISTRADO,
                "No se puede volver al estado REGISTRADO desde el estado actual"
        );
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO imprimirCredencialMasivo(CambioEstadoMasivoRequestDTO request) {
        return procesarCambioEstadoMasivoConValidacion(
                request,
                EstadoPersonal.CREDENCIAL_IMPRESO,
                "No se puede IMPRIMIR CREDENCIAL desde el estado actual"
        );
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO entregarCredencialMasivo(CambioEstadoMasivoRequestDTO request) {
        ResultadoCambioMasivoDTO resultado = procesarCambioEstadoMasivoConValidacion(
                request,
                EstadoPersonal.CREDENCIAL_ENTREGADO,
                "La credencial debe estar impresa antes de entregarla"
        );

        if (!resultado.getIdsExitosos().isEmpty()) {
            CambioEstadoMasivoRequestDTO segundoRequest = new CambioEstadoMasivoRequestDTO();
            segundoRequest.setPersonalIds(resultado.getIdsExitosos());

            ResultadoCambioMasivoDTO segundoResultado = procesarCambioEstadoMasivoConValidacion(
                    segundoRequest,
                    EstadoPersonal.PERSONAL_ACTIVO,
                    "Error al activar personal después de entregar credencial"
            );

            resultado.setExitosos(segundoResultado.getExitosos());
            resultado.setIdsExitosos(segundoResultado.getIdsExitosos());
            resultado.setPersonalesActualizados(segundoResultado.getPersonalesActualizados());
            resultado.getErrores().putAll(segundoResultado.getErrores());
            resultado.setFallidos(resultado.getErrores().size());
        }

        return resultado;
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO habilitarAccesoComputoMasivo(CambioEstadoMasivoRequestDTO request) {
        return procesarCambioEstadoMasivoConValidacion(
                request,
                EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO,
                "No se puede TENER ACCESO A COMPUTO desde el estado actual"
        );
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO devolverCredencialMasivo(CambioEstadoMasivoRequestDTO request) {
        return procesarCambioEstadoMasivoConValidacion(
                request,
                EstadoPersonal.CREDENCIAL_DEVUELTO,
                "No se puede DEVOLVER EL CREDENCIAL desde el estado actual"
        );
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO finalizarProcesoElectoralMasivo(CambioEstadoMasivoRequestDTO request) {
        return procesarCambioEstadoMasivoConValidacion(
                request,
                EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO,
                "No se puede finalizar el proceso desde el estado actual"
        );
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO contratoTerminadoMasivo(CambioEstadoMasivoRequestDTO request) {
        return procesarCambioEstadoMasivoConValidacion(
                request,
                EstadoPersonal.INACTIVO_CONTRATO_TERMINADO,
                "No se puede finalizar el contrato del personal"
        );
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO renunciarMasivo(CambioEstadoMasivoRequestDTO request) {
        return procesarCambioEstadoMasivoConValidacion(
                request,
                EstadoPersonal.INACTIVO_POR_RENUNCIA,
                "No se puede renunciar desde el estado actual"
        );
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO estadoRegistradoMasivo(CambioEstadoMasivoRequestDTO request) {
        return procesarCambioEstadoMasivoConValidacion(
                request,
                EstadoPersonal.PERSONAL_REGISTRADO,
                "No se puede volver al estado REGISTRADO desde el estado actual"
        );
    }

    /**
     * Método genérico para procesar cambios de estado masivos con validación
     * usando el método validarTransicionEstado (para estados con múltiples orígenes)
     */
    @Transactional
    protected ResultadoCambioMasivoDTO procesarCambioEstadoMasivoConValidacion(
            CambioEstadoMasivoRequestDTO request,
            EstadoPersonal nuevoEstadoEnum,
            String mensajeError) {

        ResultadoCambioMasivoDTO resultado = new ResultadoCambioMasivoDTO();
        List<Long> idsSolicitados = request.getPersonalIds();
        resultado.setTotalProcesados(idsSolicitados.size());

        List<Long> idsExitosos = new ArrayList<>();
        Map<Long, String> errores = new HashMap<>();

        try {
            Estado nuevoEstado = estadoRepository.findByEnum(nuevoEstadoEnum)
                    .orElseThrow(() -> new BusinessException(
                            "Estado " + nuevoEstadoEnum.getNombre() + " no configurado"));

            List<Personal> personales = personalRepository.findAllById(idsSolicitados);
            Set<Long> idsEncontrados = personales.stream().map(Personal::getId).collect(Collectors.toSet());

            for (Long id : idsSolicitados) {
                if (!idsEncontrados.contains(id)) {
                    errores.put(id, "Personal no encontrado");
                }
            }

            if (personales.isEmpty()) {
                return construirResultadoVacio(resultado, errores);
            }

            List<Personal> personalesValidos = new ArrayList<>();
            for (Personal personal : personales) {
                if (errores.containsKey(personal.getId())) continue;

                if (validarTransicionEstado(personal.getId(), nuevoEstadoEnum)) {
                    personalesValidos.add(personal);
                } else {
                    errores.put(personal.getId(), mensajeError);
                }
            }

            if (personalesValidos.isEmpty()) {
                return construirResultadoVacio(resultado, errores);
            }

            List<Long> idsValidos = personalesValidos.stream().map(Personal::getId).collect(Collectors.toList());

            estadoActualRepository.bulkDesactivarEstadosActuales(idsValidos);

            List<EstadoActual> nuevosEstados = personalesValidos.stream()
                    .map(personal -> EstadoActual.builder()
                            .personal(personal)
                            .estado(nuevoEstado)
                            .valor_estado_actual(true)
                            .build())
                    .collect(Collectors.toList());

            estadoActualRepository.saveAll(nuevosEstados);

            idsExitosos.addAll(idsValidos);
            resultado.setExitosos(idsExitosos.size());
            resultado.setFallidos(errores.size());
            resultado.setIdsExitosos(idsExitosos);
            resultado.setErrores(errores);
            resultado.setPersonalesActualizados(
                    personalesValidos.stream().map(this::mapToDTO).collect(Collectors.toList()));

            log.info("Batch de cambio a estado {} completado. Éxitos: {}, Fallos: {}",
                    nuevoEstadoEnum.getNombre(), idsExitosos.size(), errores.size());

        } catch (Exception e) {
            log.error("Error en procesamiento masivo con validación: {}", e.getMessage(), e);
            throw new BusinessException("Error al procesar cambio masivo: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Construye un resultado vacío con los errores proporcionados
     */
    private ResultadoCambioMasivoDTO construirResultadoVacio(
            ResultadoCambioMasivoDTO resultado,
            Map<Long, String> errores) {
        resultado.setExitosos(0);
        resultado.setFallidos(errores.size());
        resultado.setIdsExitosos(new ArrayList<>());
        resultado.setErrores(errores);
        resultado.setPersonalesActualizados(new ArrayList<>());
        return resultado;
    }

    @Override
    public boolean validarTransicionEstado(Long personalId, EstadoPersonal nuevoEstado) {
        Personal personal = validarPersonal(personalId);

        EstadoActual estadoActual = estadoActualRepository.findCurrentEstadoByPersonalId(personalId)
                .orElse(null);

        if (estadoActual == null) {
            return nuevoEstado == EstadoPersonal.PERSONAL_REGISTRADO;
        }

        String estadoActualNombre = estadoActual.getEstado().getNombre();

        switch (estadoActualNombre) {
            case "PERSONAL REGISTRADO":
                return nuevoEstado == EstadoPersonal.PERSONAL_REGISTRADO ||
                        nuevoEstado == EstadoPersonal.CREDENCIAL_IMPRESO;
            case "CREDENCIAL IMPRESO":
                return nuevoEstado == EstadoPersonal.CREDENCIAL_ENTREGADO ||
                        nuevoEstado == EstadoPersonal.INACTIVO_POR_RENUNCIA||
                        nuevoEstado == EstadoPersonal.PERSONAL_REGISTRADO;
            case "CREDENCIAL ENTREGADO":
                return nuevoEstado == EstadoPersonal.PERSONAL_ACTIVO;
            case "PERSONAL ACTIVO":
                return nuevoEstado == EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO ||
                        nuevoEstado == EstadoPersonal.CREDENCIAL_DEVUELTO;
            case "PERSONAL CON ACCESO A COMPUTO":
                return nuevoEstado == EstadoPersonal.CREDENCIAL_DEVUELTO;
            case "CREDENCIAL DEVUELTO":
                return nuevoEstado == EstadoPersonal.INACTIVO_CONTRATO_TERMINADO ||
                        nuevoEstado == EstadoPersonal.INACTIVO_POR_RENUNCIA ||
                        nuevoEstado == EstadoPersonal.PERSONAL_REGISTRADO;
            case "INACTIVO CONTRATO TERMINADO", "INACTIVO POR RENUNCIA":
                return nuevoEstado == EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO ||
                        nuevoEstado == EstadoPersonal.PERSONAL_REGISTRADO;
            case "PERSONAL INACTIVO PROCESO TERMINADO":
                return nuevoEstado == EstadoPersonal.PERSONAL_REGISTRADO;
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
        return Boolean.TRUE.equals(personal.getAccesoComputo()) &&
                estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                        personalId, EstadoPersonal.PERSONAL_ACTIVO.getNombre());
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