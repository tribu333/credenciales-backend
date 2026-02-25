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

    // --- Métodos Privados de Ayuda ---

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
        // 1. Validar que el nuevo estado existe en la BD
        Estado nuevoEstado = estadoRepository.findByEnum(nuevoEstadoEnum)
                .orElseThrow(() -> new BusinessException("Estado " + nuevoEstadoEnum.getNombre() + " no configurado en el sistema"));

        // 2. Bloquear la fila del personal para evitar lecturas concurrentes (PESIMISTIC_WRITE)
        //    Esto es clave para evitar la condición de carrera.
        Personal personal = personalRepository.findPersonalByIdWithPessimisticLock(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + personalId));

        // 3. Validar la transición (usando el método ya existente, que ahora también se beneficia del lock)
        if (!validarTransicionEstado(personalId, nuevoEstadoEnum)) {
            throw new BusinessException(reglaValidacionMensaje);
        }

        // 4. Desactivar el estado actual (si existe)
        estadoActualRepository.findCurrentEstadoByPersonalId(personalId)
                .ifPresent(estadoActual -> {
                    estadoActual.setValor_estado_actual(false);
                    estadoActualRepository.save(estadoActual); // Este save está dentro de la misma tx
                });

        // 5. Crear el nuevo estado
        EstadoActual nuevoEstadoActual = EstadoActual.builder()
                .personal(personal)
                .estado(nuevoEstado)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(nuevoEstadoActual);
        log.info("Personal ID {} cambió a estado {}", personalId, nuevoEstadoEnum.getNombre());

        return mapToDTO(personal);
    }

    // --- Métodos de Cambio de Estado (ahora usan el método genérico) ---

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
        // Lógica especial: primero a CREDENCIAL ENTREGADO, luego a PERSONAL ACTIVO
        Personal personal = personalRepository.findPersonalByIdWithPessimisticLock(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + personalId));

        if (!validarTransicionEstado(personalId, EstadoPersonal.CREDENCIAL_ENTREGADO)) {
            throw new BusinessException("La credencial debe estar impresa antes de entregarla");
        }

        Estado estadoEntregado = estadoRepository.findByEnum(EstadoPersonal.CREDENCIAL_ENTREGADO)
                .orElseThrow(() -> new BusinessException("Estado CREDENCIAL ENTREGADO no configurado"));
        Estado estadoActivo = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_ACTIVO)
                .orElseThrow(() -> new BusinessException("Estado PERSONAL ACTIVO no configurado"));

        // Desactivar actual (CREDENCIAL IMPRESO)
        desactivarEstadoActual(personalId);

        // Crear CREDENCIAL ENTREGADO
        EstadoActual entregado = EstadoActual.builder()
                .personal(personal)
                .estado(estadoEntregado)
                .valor_estado_actual(true)
                .build();
        estadoActualRepository.save(entregado);

        // Desactivar CREDENCIAL ENTREGADO y crear PERSONAL ACTIVO (inmediato)
        // Nota: En un flujo real, tal vez quieras dejar un tiempo entre estos dos.
        // Por simplicidad, lo hacemos automático como en tu código original.
        desactivarEstadoActual(personalId); // Desactiva el que acabamos de crear (está bien porque está en la misma tx)

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
        if (Boolean.FALSE.equals(personal.getAccesoComputo())) {
            throw new BusinessException("Este personal no tiene habilitado el acceso a cómputo");
        }
        return cambiarEstadoPersonal(
                personalId,
                EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO,
                "El personal debe estar ACTIVO para habilitar acceso a cómputo"
        );
    }

    @Override
    public PersonalDTO devolverCredencial(Long personalId) {
        // Lógica especial: Primero CREDENCIAL DEVUELTO, luego INACTIVO
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

        // Llamar a finalizar proceso (que ya maneja su propio lock, pero como estamos dentro de la misma tx, el lock ya está adquirido)
        return finalizarProcesoElectoral(personalId);
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
        // TODO: Revisar la lógica de este método. El nombre y la implementación actual no coinciden.
        // Por ahora, lo dejo como estaba pero usando el lock.
        Personal personal = personalRepository.findPersonalByIdWithPessimisticLock(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado"));

        boolean puedeVolverARegistrarse = estadoActualRepository.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
                personalId, EstadoPersonal.CREDENCIAL_IMPRESO.getNombre());

        if (!puedeVolverARegistrarse) {
            throw new BusinessException("No puede volver a imprimir, tiene que devolver la credencial.");
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
        log.info("Se habilitó para volver a imprimir la credencial a personal con id: {}", personalId);

        return mapToDTO(personal);
    }


    // --- Métodos de Validación (sin cambios significativos) ---

    @Override
    public boolean validarTransicionEstado(Long personalId, EstadoPersonal nuevoEstado) {
        // Este método ahora se llama dentro del bloqueo pesimista, por lo que la lectura del estado actual es segura.
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
                return nuevoEstado == EstadoPersonal.CREDENCIAL_IMPRESO ||
                        nuevoEstado == EstadoPersonal.INACTIVO_POR_RENUNCIA;
            case "CREDENCIAL IMPRESO":
                return nuevoEstado == EstadoPersonal.CREDENCIAL_ENTREGADO ||
                        nuevoEstado == EstadoPersonal.INACTIVO_POR_RENUNCIA;
            case "CREDENCIAL ENTREGADO":
                return nuevoEstado == EstadoPersonal.PERSONAL_ACTIVO;
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

    // --- Métodos de Consulta ---
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

    // --- MÉTODO MASIVO OPTIMIZADO ---
    @Override
    @Transactional
    public ResultadoCambioMasivoDTO imprimirCredencialMasivo(CambioEstadoMasivoRequestDTO request) {
        ResultadoCambioMasivoDTO resultado = new ResultadoCambioMasivoDTO();
        List<Long> idsSolicitados = request.getPersonalIds();
        resultado.setTotalProcesados(idsSolicitados.size());

        List<Long> idsExitosos = new ArrayList<>();
        Map<Long, String> errores = new HashMap<>();

        // 1. Obtener el estado "CREDENCIAL IMPRESO" una sola vez
        Estado estadoImpreso = estadoRepository.findByEnum(EstadoPersonal.CREDENCIAL_IMPRESO)
                .orElseThrow(() -> new BusinessException("Estado CREDENCIAL IMPRESO no configurado"));

        // 2. Validar que todos los personales existen y están en estado correcto (PERSONAL_REGISTRADO)
        //    Usamos una sola query para traer todos los personales y sus estados actuales.
        List<Personal> personales = personalRepository.findAllById(idsSolicitados);
        Set<Long> idsEncontrados = personales.stream().map(Personal::getId).collect(Collectors.toSet());

        // IDs no encontrados
        for (Long id : idsSolicitados) {
            if (!idsEncontrados.contains(id)) {
                errores.put(id, "Personal no encontrado");
            }
        }

        if (personales.isEmpty()) {
            resultado.setExitosos(0);
            resultado.setFallidos(errores.size());
            resultado.setErrores(errores);
            return resultado;
        }

        // 3. Obtener los estados actuales de todos estos personales en UNA SOLA QUERY
        List<EstadoActual> estadosActuales = estadoActualRepository.findAllCurrentEstadosByPersonalIds(new ArrayList<>(idsEncontrados));
        Map<Long, EstadoActual> estadoActualMap = estadosActuales.stream()
                .collect(Collectors.toMap(ea -> ea.getPersonal().getId(), ea -> ea));

        // 4. Validar regla de negocio para cada uno (que su estado actual sea PERSONAL REGISTRADO)
        List<Personal> personalesValidos = new ArrayList<>();
        for (Personal personal : personales) {
            EstadoActual estadoActual = estadoActualMap.get(personal.getId());
            if (estadoActual == null) {
                errores.put(personal.getId(), "El personal no tiene un estado actual asignado");
            } else if (!estadoActual.getEstado().getNombre().equals(EstadoPersonal.PERSONAL_REGISTRADO.getNombre())) {
                errores.put(personal.getId(), "El personal debe estar en estado REGISTRADO para imprimir credencial");
            } else {
                personalesValidos.add(personal);
            }
        }

        if (personalesValidos.isEmpty()) {
            resultado.setExitosos(0);
            resultado.setFallidos(errores.size());
            resultado.setErrores(errores);
            return resultado;
        }

        List<Long> idsValidos = personalesValidos.stream().map(Personal::getId).collect(Collectors.toList());

        // 5. Operación Masiva Atómica: Desactivar todos los estados actuales de los válidos en un solo UPDATE
        int registrosActualizados = estadoActualRepository.bulkDesactivarEstadosActuales(idsValidos);
        log.info("Desactivados {} estados actuales para el batch de impresión", registrosActualizados);

        // 6. Crear y guardar todos los nuevos estados en un solo BATCH INSERT
        List<EstadoActual> nuevosEstados = personalesValidos.stream()
                .map(personal -> EstadoActual.builder()
                        .personal(personal)
                        .estado(estadoImpreso)
                        .valor_estado_actual(true)
                        .build())
                .collect(Collectors.toList());

        // Guardar todos en batch (JPA hará un batch insert gracias a la propiedad hibernate.jdbc.batch_size)
        estadoActualRepository.saveAll(nuevosEstados);

        // 7. Preparar resultado
        idsExitosos.addAll(idsValidos);
        resultado.setExitosos(idsExitosos.size());
        resultado.setFallidos(errores.size());
        resultado.setIdsExitosos(idsExitosos);
        resultado.setErrores(errores);
        resultado.setPersonalesActualizados(personalesValidos.stream().map(this::mapToDTO).collect(Collectors.toList()));

        log.info("Batch de impresión completado. Éxitos: {}, Fallos: {}", idsExitosos.size(), errores.size());
        return resultado;
    }

    // --- Métodos de Mapeo y ayuda ---
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