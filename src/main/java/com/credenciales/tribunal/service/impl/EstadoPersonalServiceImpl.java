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
        Personal personal = personalRepository.findPersonalByIdWithPessimisticLock(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + personalId));

        if (!validarTransicionEstado(personalId, EstadoPersonal.CREDENCIAL_ENTREGADO)) {
            throw new BusinessException("La credencial debe estar impresa antes de entregarla");
        }

        Estado estadoEntregado = estadoRepository.findByEnum(EstadoPersonal.CREDENCIAL_ENTREGADO)
                .orElseThrow(() -> new BusinessException("Estado CREDENCIAL ENTREGADO no configurado"));
        Estado estadoActivo = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_ACTIVO)
                .orElseThrow(() -> new BusinessException("Estado PERSONAL ACTIVO no configurado"));

        desactivarEstadoActual(personalId);

        EstadoActual entregado = EstadoActual.builder()
                .personal(personal)
                .estado(estadoEntregado)
                .valor_estado_actual(true)
                .build();
        estadoActualRepository.save(entregado);

        desactivarEstadoActual(personalId);

        EstadoActual activo = EstadoActual.builder()
                .personal(personal)
                .estado(estadoActivo)
                .valor_estado_actual(true)
                .build();
        estadoActualRepository.save(activo);

        log.info("Credencial entregada y personal {} activado", personalId);
        return mapToDTO(personal);
    }

    @Override
    public PersonalDTO habilitarAccesoComputo(Long personalId) {
        Personal personal = validarPersonal(personalId);

        boolean puedeTenerAccesoAComputo =
                estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                        personalId, EstadoPersonal.PERSONAL_ACTIVO.getNombre()) ;

        if (!puedeTenerAccesoAComputo) {
            throw new BusinessException("Para tener Acceso a Computo tiene que ser Personal Activo.");
        }

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
        Personal personal = personalRepository.findPersonalByIdWithPessimisticLock(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + personalId));

        boolean esActivoOVálido = validarTransicionEstado(personalId, EstadoPersonal.CREDENCIAL_DEVUELTO);
        if (!esActivoOVálido) {
            throw new BusinessException("El personal debe estar ACTIVO o con ACCESO A COMPUTO para devolver credencial");
        }

        Estado estadoDevuelto = estadoRepository.findByEnum(EstadoPersonal.CREDENCIAL_DEVUELTO)
                .orElseThrow(() -> new BusinessException("Estado CREDENCIAL DEVUELTO no configurado"));

        desactivarEstadoActual(personalId);
        EstadoActual devuelto = EstadoActual.builder()
                .personal(personal)
                .estado(estadoDevuelto)
                .valor_estado_actual(true)
                .build();
        estadoActualRepository.save(devuelto);
        log.info("Credencial devuelta para personal ID: {}. Estado actual: CREDENCIAL DEVUELTO", personalId);

        return mapToDTO(personal);
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
    public PersonalDTO renunciar(Long personalId) {
        return cambiarEstadoPersonal(
                personalId,
                EstadoPersonal.INACTIVO_POR_RENUNCIA,
                "No se puede renunciar desde el estado actual"
        );
    }

    @Override
    public PersonalDTO estadoRegistrado(Long personalId) {
        Personal personal = personalRepository.findPersonalByIdWithPessimisticLock(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado"));

        boolean puedeVolverARegistrarse =
                estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                        personalId, EstadoPersonal.CREDENCIAL_IMPRESO.getNombre()) ||
                        estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                                personalId, EstadoPersonal.CREDENCIAL_DEVUELTO.getNombre()) ||
                        estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                                personalId, EstadoPersonal.PERSONAL_REGISTRADO.getNombre());

        if (!puedeVolverARegistrarse) {
            throw new BusinessException("No puede volver a imprimir, tiene que devolver la credencial.");
        }

        Estado estado = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_REGISTRADO)
                .orElseThrow(() -> new BusinessException("Estado PERSONAL REGISTRADO no configurado."));

        desactivarEstadoActual(personalId);

        EstadoActual nuevoEstado = EstadoActual.builder()
                .personal(personal)
                .estado(estado)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(nuevoEstado);
        log.info("Personal ID: {} volvió a estado REGISTRADO desde estado anterior", personalId);

        return mapToDTO(personal);
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO imprimirCredencialMasivo(CambioEstadoMasivoRequestDTO request) {
        return procesarCambioEstadoSimpleMasivo(
                request,
                EstadoPersonal.CREDENCIAL_IMPRESO,
                EstadoPersonal.PERSONAL_REGISTRADO,
                "El personal debe estar en estado REGISTRADO para imprimir credencial"
        );
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO entregarCredencialMasivo(CambioEstadoMasivoRequestDTO request) {
        ResultadoCambioMasivoDTO resultado = new ResultadoCambioMasivoDTO();
        List<Long> idsSolicitados = request.getPersonalIds();
        resultado.setTotalProcesados(idsSolicitados.size());

        List<Long> idsExitosos = new ArrayList<>();
        Map<Long, String> errores = new HashMap<>();

        try {
            Estado estadoEntregado = estadoRepository.findByEnum(EstadoPersonal.CREDENCIAL_ENTREGADO)
                    .orElseThrow(() -> new BusinessException("Estado CREDENCIAL ENTREGADO no configurado"));
            Estado estadoActivo = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_ACTIVO)
                    .orElseThrow(() -> new BusinessException("Estado PERSONAL ACTIVO no configurado"));

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

            List<EstadoActual> estadosActuales = estadoActualRepository
                    .findAllCurrentEstadosByPersonalIds(new ArrayList<>(idsEncontrados));

            Map<Long, EstadoActual> estadoActualMap = estadosActuales.stream()
                    .collect(Collectors.toMap(ea -> ea.getPersonal().getId(), ea -> ea));

            List<Personal> personalesValidos = new ArrayList<>();
            for (Personal personal : personales) {
                if (errores.containsKey(personal.getId())) continue;

                EstadoActual ea = estadoActualMap.get(personal.getId());
                if (ea == null) {
                    errores.put(personal.getId(), "El personal no tiene un estado actual asignado");
                } else if (!ea.getEstado().getNombre().equals(EstadoPersonal.CREDENCIAL_IMPRESO.getNombre())) {
                    errores.put(personal.getId(), "La credencial debe estar impresa antes de entregarla");
                } else {
                    personalesValidos.add(personal);
                }
            }

            if (personalesValidos.isEmpty()) {
                return construirResultadoVacio(resultado, errores);
            }

            List<Long> idsValidos = personalesValidos.stream().map(Personal::getId).collect(Collectors.toList());

            estadoActualRepository.bulkDesactivarEstadosActuales(idsValidos);

            List<EstadoActual> estadosEntregados = personalesValidos.stream()
                    .map(personal -> EstadoActual.builder()
                            .personal(personal)
                            .estado(estadoEntregado)
                            .valor_estado_actual(true)
                            .build())
                    .collect(Collectors.toList());
            estadoActualRepository.saveAll(estadosEntregados);

            estadoActualRepository.bulkDesactivarEstadosActuales(idsValidos);

            List<EstadoActual> estadosActivos = personalesValidos.stream()
                    .map(personal -> EstadoActual.builder()
                            .personal(personal)
                            .estado(estadoActivo)
                            .valor_estado_actual(true)
                            .build())
                    .collect(Collectors.toList());
            estadoActualRepository.saveAll(estadosActivos);

            idsExitosos.addAll(idsValidos);
            resultado.setExitosos(idsExitosos.size());
            resultado.setFallidos(errores.size());
            resultado.setIdsExitosos(idsExitosos);
            resultado.setErrores(errores);
            resultado.setPersonalesActualizados(personalesValidos.stream().map(this::mapToDTO).collect(Collectors.toList()));

            log.info("Batch de entrega de credenciales completado. Éxitos: {}, Fallos: {}",
                    idsExitosos.size(), errores.size());

        } catch (Exception e) {
            log.error("Error en entregarCredencialMasivo: {}", e.getMessage(), e);
            throw new BusinessException("Error al procesar entrega masiva: " + e.getMessage());
        }
        return resultado;
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO habilitarAccesoComputoMasivo(CambioEstadoMasivoRequestDTO request) {
        ResultadoCambioMasivoDTO resultado = new ResultadoCambioMasivoDTO();
        List<Long> idsSolicitados = request.getPersonalIds();
        resultado.setTotalProcesados(idsSolicitados.size());

        List<Long> idsExitosos = new ArrayList<>();
        Map<Long, String> errores = new HashMap<>();

        try {
            Estado estadoComputo = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO)
                    .orElseThrow(() -> new BusinessException("Estado PERSONAL CON ACCESO A COMPUTO no configurado"));

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

            List<EstadoActual> estadosActuales = estadoActualRepository
                    .findAllCurrentEstadosByPersonalIds(new ArrayList<>(idsEncontrados));

            Map<Long, EstadoActual> estadoActualMap = estadosActuales.stream()
                    .collect(Collectors.toMap(ea -> ea.getPersonal().getId(), ea -> ea));

            List<Personal> personalesValidos = new ArrayList<>();
            for (Personal personal : personales) {
                if (errores.containsKey(personal.getId())) continue;

                EstadoActual ea = estadoActualMap.get(personal.getId());
                if (ea == null) {
                    errores.put(personal.getId(), "El personal no tiene un estado actual asignado");
                } else if (!ea.getEstado().getNombre().equals(EstadoPersonal.PERSONAL_ACTIVO.getNombre())) {
                    errores.put(personal.getId(), "El personal debe estar ACTIVO para habilitar acceso a cómputo");
                } else if (Boolean.FALSE.equals(personal.getAccesoComputo())) {
                    errores.put(personal.getId(), "El personal no tiene habilitado el acceso a cómputo");
                } else {
                    personalesValidos.add(personal);
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
                            .estado(estadoComputo)
                            .valor_estado_actual(true)
                            .build())
                    .collect(Collectors.toList());
            estadoActualRepository.saveAll(nuevosEstados);

            idsExitosos.addAll(idsValidos);
            resultado.setExitosos(idsExitosos.size());
            resultado.setFallidos(errores.size());
            resultado.setIdsExitosos(idsExitosos);
            resultado.setErrores(errores);
            resultado.setPersonalesActualizados(personalesValidos.stream().map(this::mapToDTO).collect(Collectors.toList()));

            log.info("Batch de habilitación de acceso a cómputo completado. Éxitos: {}, Fallos: {}",
                    idsExitosos.size(), errores.size());

        } catch (Exception e) {
            log.error("Error en habilitarAccesoComputoMasivo: {}", e.getMessage(), e);
            throw new BusinessException("Error al procesar habilitación masiva: " + e.getMessage());
        }

        return resultado;
    }

    @Override
    @Transactional
    public ResultadoCambioMasivoDTO devolverCredencialMasivo(CambioEstadoMasivoRequestDTO request) {
        ResultadoCambioMasivoDTO resultado = new ResultadoCambioMasivoDTO();
        List<Long> idsSolicitados = request.getPersonalIds();
        resultado.setTotalProcesados(idsSolicitados.size());

        List<Long> idsExitosos = new ArrayList<>();
        Map<Long, String> errores = new HashMap<>();

        try {
            Estado estadoDevuelto = estadoRepository.findByEnum(EstadoPersonal.CREDENCIAL_DEVUELTO)
                    .orElseThrow(() -> new BusinessException("Estado CREDENCIAL DEVUELTO no configurado"));
            /*stado estadoInactivo = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO)
                    .orElseThrow(() -> new BusinessException("Estado INACTIVO PROCESO TERMINADO no configurado"));*/

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

            List<EstadoActual> estadosActuales = estadoActualRepository
                    .findAllCurrentEstadosByPersonalIds(new ArrayList<>(idsEncontrados));

            Map<Long, EstadoActual> estadoActualMap = estadosActuales.stream()
                    .collect(Collectors.toMap(ea -> ea.getPersonal().getId(), ea -> ea));

            List<Personal> personalesValidos = new ArrayList<>();
            for (Personal personal : personales) {
                if (errores.containsKey(personal.getId())) continue;

                EstadoActual ea = estadoActualMap.get(personal.getId());
                if (ea == null) {
                    errores.put(personal.getId(), "El personal no tiene un estado actual asignado");
                } else {
                    String estadoNombre = ea.getEstado().getNombre();
                    if (estadoNombre.equals(EstadoPersonal.PERSONAL_ACTIVO.getNombre()) ||
                            estadoNombre.equals(EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO.getNombre())) {
                        personalesValidos.add(personal);
                    } else {
                        errores.put(personal.getId(),
                                "El personal debe estar ACTIVO o con ACCESO A COMPUTO para devolver credencial");
                    }
                }
            }

            if (personalesValidos.isEmpty()) {
                return construirResultadoVacio(resultado, errores);
            }

            List<Long> idsValidos = personalesValidos.stream().map(Personal::getId).collect(Collectors.toList());

            estadoActualRepository.bulkDesactivarEstadosActuales(idsValidos);

            List<EstadoActual> estadosDevueltos = personalesValidos.stream()
                    .map(personal -> EstadoActual.builder()
                            .personal(personal)
                            .estado(estadoDevuelto)
                            .valor_estado_actual(true)
                            .build())
                    .collect(Collectors.toList());
            estadoActualRepository.saveAll(estadosDevueltos);

            // Desactivar CREDENCIAL DEVUELTO y crear INACTIVO
            /*estadoActualRepository.bulkDesactivarEstadosActuales(idsValidos);

            List<EstadoActual> estadosInactivos = personalesValidos.stream()
                    .map(personal -> EstadoActual.builder()
                            .personal(personal)
                            .estado(estadoInactivo)
                            .valor_estado_actual(true)
                            .build())
                    .collect(Collectors.toList());
            estadoActualRepository.saveAll(estadosInactivos);*/

            idsExitosos.addAll(idsValidos);
            resultado.setExitosos(idsExitosos.size());
            resultado.setFallidos(errores.size());
            resultado.setIdsExitosos(idsExitosos);
            resultado.setErrores(errores);
            resultado.setPersonalesActualizados(personalesValidos.stream().map(this::mapToDTO).collect(Collectors.toList()));

            log.info("Batch de devolución de credenciales completado. Éxitos: {}, Fallos: {}",
                    idsExitosos.size(), errores.size());

        } catch (Exception e) {
            log.error("Error en devolverCredencialMasivo: {}", e.getMessage(), e);
            throw new BusinessException("Error al procesar devolución masiva: " + e.getMessage());
        }

        return resultado;
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
        ResultadoCambioMasivoDTO resultado = new ResultadoCambioMasivoDTO();
        List<Long> idsSolicitados = request.getPersonalIds();
        resultado.setTotalProcesados(idsSolicitados.size());

        List<Long> idsExitosos = new ArrayList<>();
        Map<Long, String> errores = new HashMap<>();

        try {
            Estado estado = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_REGISTRADO)
                    .orElseThrow(() -> new BusinessException("Estado PERSONAL REGISTRADO no configurado"));

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

            // Obtener TODOS los estados actuales de las personas encontradas
            List<EstadoActual> estadosActuales = estadoActualRepository
                    .findAllCurrentEstadosByPersonalIds(new ArrayList<>(idsEncontrados));

            // Definir los estados permitidos para volver a REGISTRADO
            Set<String> estadosPermitidos = Set.of(
                    EstadoPersonal.CREDENCIAL_IMPRESO.getNombre(),
                    EstadoPersonal.PERSONAL_REGISTRADO.getNombre(),
                    EstadoPersonal.CREDENCIAL_DEVUELTO.getNombre()
            );

            // Mapear personal ID a su estado actual
            Map<Long, EstadoActual> estadoActualMap = estadosActuales.stream()
                    .collect(Collectors.toMap(ea -> ea.getPersonal().getId(), ea -> ea));

            List<Personal> personalesValidos = new ArrayList<>();
            for (Personal personal : personales) {
                if (errores.containsKey(personal.getId())) continue;

                EstadoActual ea = estadoActualMap.get(personal.getId());

                if (ea == null) {
                    errores.put(personal.getId(), "El personal no tiene un estado actual asignado");
                } else {
                    String estadoActualNombre = ea.getEstado().getNombre();

                    // Validar si el estado actual está en la lista de permitidos
                    if (estadosPermitidos.contains(estadoActualNombre)) {
                        personalesValidos.add(personal);
                    } else {
                        errores.put(personal.getId(),
                                String.format("No puede volver a registrarse. Estados permitidos: %s, %s, %s. Estado actual: %s",
                                        EstadoPersonal.CREDENCIAL_IMPRESO.getNombre(),
                                        EstadoPersonal.PERSONAL_REGISTRADO.getNombre(),
                                        EstadoPersonal.CREDENCIAL_DEVUELTO.getNombre(),
                                        estadoActualNombre));
                    }
                }
            }

            if (personalesValidos.isEmpty()) {
                return construirResultadoVacio(resultado, errores);
            }

            List<Long> idsValidos = personalesValidos.stream().map(Personal::getId).collect(Collectors.toList());

            // Desactivar estados actuales
            estadoActualRepository.bulkDesactivarEstadosActuales(idsValidos);

            // Crear nuevos estados REGISTRADO
            List<EstadoActual> nuevosEstados = personalesValidos.stream()
                    .map(personal -> EstadoActual.builder()
                            .personal(personal)
                            .estado(estado)
                            .valor_estado_actual(true)
                            .build())
                    .collect(Collectors.toList());
            estadoActualRepository.saveAll(nuevosEstados);

            idsExitosos.addAll(idsValidos);
            resultado.setExitosos(idsExitosos.size());
            resultado.setFallidos(errores.size());
            resultado.setIdsExitosos(idsExitosos);
            resultado.setErrores(errores);
            resultado.setPersonalesActualizados(personalesValidos.stream().map(this::mapToDTO).collect(Collectors.toList()));

            log.info("Batch de estado registrado completado. Éxitos: {}, Fallos: {}",
                    idsExitosos.size(), errores.size());

        } catch (Exception e) {
            log.error("Error en estadoRegistradoMasivo: {}", e.getMessage(), e);
            throw new BusinessException("Error al procesar estado registrado masivo: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Método genérico para procesar cambios de estado masivos simples
     * (donde solo se requiere un estado origen específico)
     */
    @Transactional
    protected ResultadoCambioMasivoDTO procesarCambioEstadoSimpleMasivo(
            CambioEstadoMasivoRequestDTO request,
            EstadoPersonal nuevoEstadoEnum,
            EstadoPersonal estadoRequeridoEnum,
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

            List<EstadoActual> estadosActuales = estadoActualRepository
                    .findAllCurrentEstadosByPersonalIds(new ArrayList<>(idsEncontrados));

            Map<Long, EstadoActual> estadoActualMap = estadosActuales.stream()
                    .collect(Collectors.toMap(ea -> ea.getPersonal().getId(), ea -> ea));

            List<Personal> personalesValidos = new ArrayList<>();
            for (Personal personal : personales) {
                if (errores.containsKey(personal.getId())) continue;

                EstadoActual ea = estadoActualMap.get(personal.getId());
                if (ea == null) {
                    errores.put(personal.getId(), "El personal no tiene un estado actual asignado");
                } else if (!ea.getEstado().getNombre().equals(estadoRequeridoEnum.getNombre())) {
                    errores.put(personal.getId(), mensajeError);
                } else {
                    personalesValidos.add(personal);
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
            log.error("Error en procesamiento masivo: {}", e.getMessage(), e);
            throw new BusinessException("Error al procesar cambio masivo: " + e.getMessage());
        }

        return resultado;
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
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado"));

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
                        nuevoEstado == EstadoPersonal.INACTIVO_POR_RENUNCIA;
            case "CREDENCIAL ENTREGADO":
                return nuevoEstado == EstadoPersonal.PERSONAL_ACTIVO;
            case "PERSONAL ACTIVO":
                return nuevoEstado == EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO ||
                        nuevoEstado == EstadoPersonal.CREDENCIAL_DEVUELTO;
            case "PERSONAL CON ACCESO A COMPUTO":
                return nuevoEstado == EstadoPersonal.CREDENCIAL_DEVUELTO;
            case "CREDENCIAL DEVUELTO":
                return nuevoEstado == EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO ||
                        nuevoEstado == EstadoPersonal.INACTIVO_POR_RENUNCIA;
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