package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.email.VerificacionCodigoRequestDTO;
import com.credenciales.tribunal.dto.email.VerificacionEmailRequestDTO;
import com.credenciales.tribunal.dto.email.VerificacionResponseDTO;
import com.credenciales.tribunal.dto.personal.*;
import com.credenciales.tribunal.dto.qr.QrGenerarDTO;
import com.credenciales.tribunal.dto.qr.QrResponseDTO;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.model.entity.*;
import com.credenciales.tribunal.model.enums.EstadoPersonal;
import com.credenciales.tribunal.model.enums.EstadoQr;
import com.credenciales.tribunal.model.enums.TipoPersonal;
import com.credenciales.tribunal.model.enums.TipoQr;
import com.credenciales.tribunal.repository.*;
import com.credenciales.tribunal.service.EmailService;
import com.credenciales.tribunal.service.PersonalService;
import com.credenciales.tribunal.service.QrService;
import com.credenciales.tribunal.service.ImagenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.util.function.Function;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PersonalServiceImpl implements PersonalService {
    @Value("${qr.base-url-images}")
    private String baseUrl;

    private final PersonalRepository personalRepository;
    private final EstadoActualRepository estadoActualRepository;
    private final EstadoRepository estadoRepository;
    private final VerificacionEmailRepository verificacionEmailRepository;
    private final QrService qrService;
    private final QrRepository qrRepository;
    private final ImagenService imagenService;
    private final EmailService emailService;
    private final HistorialCargoRepository historialCargoRepository;
    private final HistorialCargoProcesoRepository historialCargoProcesoRepository;
    private final CargoRepository cargoRepository;
    private final CargoProcesoRepository cargoProcesoRepository;

    private static final int EXPIRACION_MINUTOS = 5;

    @Override
    public VerificacionResponseDTO solicitarCodigoVerificacion(VerificacionEmailRequestDTO request) {

        log.info("Solicitando código para email: {}, CI: {}", request.getCorreo(), request.getCarnetIdentidad());

        // Validar si el correo ya existe en personal activo
        if (existeCorreoActivo(request.getCorreo())) {
            throw new BusinessException("El correo electrónico ya está registrado por otro personal activo");
        }

        // Verificar si puede registrarse nuevamente (si el CI ya existe)
        String mensajeEstado = obtenerMensajeEstadoActual(request.getCarnetIdentidad());
        if (mensajeEstado != null) {
            throw new BusinessException(mensajeEstado);
        }

        // Generar código aleatorio de 6 dígitos
        String codigo = generarCodigoVerificacion();

        // Guardar en base de datos
        VerificacionEmail verificacion = VerificacionEmail.builder()
                .email(request.getCorreo())
                .codigo(codigo)
                .carnetIdentidad(request.getCarnetIdentidad())
                .fechaExpiracion(LocalDateTime.now().plusMinutes(EXPIRACION_MINUTOS))
                .utilizado(false)
                .build();

        verificacionEmailRepository.save(verificacion);

        // Enviar código por email
        emailService.enviarCodigoVerificacion(request.getCorreo(), codigo, request.getCarnetIdentidad());

        // Limpiar códigos expirados
        verificacionEmailRepository.eliminarExpirados(LocalDateTime.now());

        log.info("Código de verificación enviado a: {} para CI: {}", request.getCorreo(), request.getCarnetIdentidad());

        return VerificacionResponseDTO.builder()
                .mensaje("Código de verificación enviado a " + request.getCorreo())
                .email(request.getCorreo())
                .expiracionMinutos(EXPIRACION_MINUTOS)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @Override
    public boolean verificarCodigo(VerificacionCodigoRequestDTO request) {
        VerificacionEmail verificacion = verificacionEmailRepository
                .findTopByEmailAndCarnetIdentidadAndUtilizadoFalseOrderByCreatedAtDesc(
                        request.getCorreo(), request.getCarnetIdentidad())
                .orElseThrow(() -> new BusinessException("No hay código de verificación pendiente para este email"));

        if (verificacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BusinessException("El código ha expirado. Solicite uno nuevo.");
        }

        if (!verificacion.getCodigo().equals(request.getCodigo())) {
            throw new BusinessException("Código de verificación incorrecto");
        }

        verificacion.setUtilizado(true);
        verificacionEmailRepository.save(verificacion);

        log.info("Código verificado correctamente para: {}", request.getCorreo());

        return true;
    }

    @Override
    public PersonalCompletoDTO registrarPersonalCompleto(
            PersonalCreateDTO registroDTO) {

        // Verificar código
        VerificacionCodigoRequestDTO verifRequest = VerificacionCodigoRequestDTO.builder()
                .correo(registroDTO.getCorreo())
                .carnetIdentidad(registroDTO.getCarnetIdentidad())
                .codigo(registroDTO.getCodigoVerificacion())
                .build();

        if (!verificarCodigo(verifRequest)) {
            throw new BusinessException("Código de verificación inválido");
        }

        // Verificar si el CI ya existe y manejar según estado
        Personal personalExistente = personalRepository.findByCarnetIdentidad(registroDTO.getCarnetIdentidad())
                .orElse(null);

        if (personalExistente != null) {
            return actualizarPersonalExistente(personalExistente, registroDTO);
        }

        // Crear nuevo personal
        return crearNuevoPersonal(registroDTO);
    }

    private PersonalCompletoDTO crearNuevoPersonal(PersonalCreateDTO registroDTO) {

        // 1. Obtener la imagen por ID
        Imagen imagen = imagenService.findEntityById(registroDTO.getImagenId());
        if (imagen == null) {
            throw new BusinessException("Imagen no encontrada con ID: " + registroDTO.getImagenId());
        }

        // 2. Generar QR
        Qr qr = generarQrParaPersonal(registroDTO.getCarnetIdentidad());

        // 3. Crear personal
        Personal personal = Personal.builder()
                .nombre(registroDTO.getNombre())
                .apellidoPaterno(registroDTO.getApellidoPaterno())
                .apellidoMaterno(registroDTO.getApellidoMaterno())
                .carnetIdentidad(registroDTO.getCarnetIdentidad())
                .correo(registroDTO.getCorreo())
                .celular(registroDTO.getCelular())
                .accesoComputo(registroDTO.getAccesoComputo() != null ? registroDTO.getAccesoComputo() : false)
                .nroCircunscripcion(registroDTO.getNroCircunscripcion())
                .tipo(registroDTO.getTipo())
                .imagen(imagen)
                .qr(qr)
                .tokenPersonal(generarTokenPersonal())
                .build();

        personal = personalRepository.save(personal);

        // 4. Asignar QR al personal
        qr.setPersonal(personal);
        qr.setEstado(EstadoQr.ASIGNADO);
        qrRepository.save(qr);

        // 5. Registrar estado inicial
        registrarEstadoInicial(personal);

        // 6. Registrar cargo según tipo
        if (registroDTO.getTipo() == TipoPersonal.PLANTA) {
            // Para PLANTA, usamos cargoID
            if (registroDTO.getCargoID() == null) {
                throw new BusinessException("Para personal de PLANTA debe especificar un cargo (cargoID)");
            }
            registrarCargoPlanta(personal, registroDTO.getCargoID());
        } else {
            // Para EVENTUAL, también usamos cargoID (según tu DTO)
            if (registroDTO.getCargoID() == null) {
                throw new BusinessException("Para personal EVENTUAL debe especificar un cargo de proceso (cargoID)");
            }
            registrarCargoEventual(personal, registroDTO.getCargoID());
        }

        log.info("Personal creado exitosamente: {} - {}", personal.getId(), personal.getCarnetIdentidad());

        return mapToCompletoDTO(personal);
    }

    private PersonalCompletoDTO actualizarPersonalExistente(
            Personal personalExistente,
            PersonalCreateDTO registroDTO) {

        // Obtener estado actual
        String estadoActual = obtenerEstadoActual(personalExistente.getId());

        // Validar según el estado
        if (EstadoPersonal.CREDENCIAL_ENTREGADO.getNombre().equals(estadoActual) ||
                EstadoPersonal.PERSONAL_ACTIVO.getNombre().equals(estadoActual) ||
                EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO.getNombre().equals(estadoActual)) {
            throw new BusinessException(
                    "El personal tiene la credencial entregada y activa. " +
                            "Debe devolver la credencial antes de poder registrarse nuevamente.");
        }

        // Actualizar datos básicos
        personalExistente.setNombre(registroDTO.getNombre());
        personalExistente.setApellidoPaterno(registroDTO.getApellidoPaterno());
        personalExistente.setApellidoMaterno(registroDTO.getApellidoMaterno());
        personalExistente.setCorreo(registroDTO.getCorreo());
        personalExistente.setCelular(registroDTO.getCelular());
        personalExistente
                .setAccesoComputo(registroDTO.getAccesoComputo() != null ? registroDTO.getAccesoComputo() : false);
        personalExistente.setNroCircunscripcion(registroDTO.getNroCircunscripcion());

        // Actualizar imagen si se proporciona un nuevo ID
        if (registroDTO.getImagenId() != null) {
            Imagen nuevaImagen = imagenService.findEntityById(registroDTO.getImagenId());
            personalExistente.setImagen(nuevaImagen);
        }

        // Generar nuevo QR si es necesario
        if (personalExistente.getQr() == null ||
                personalExistente.getQr().getEstado() == EstadoQr.INACTIVO) {
            Qr nuevoQr = generarQrParaPersonal(registroDTO.getCarnetIdentidad());
            nuevoQr.setPersonal(personalExistente);
            nuevoQr.setEstado(EstadoQr.ASIGNADO);
            qrRepository.save(nuevoQr);
            personalExistente.setQr(nuevoQr);
        }

        personalExistente = personalRepository.save(personalExistente);

        // Si está inactivo, reactivar con estado REGISTRADO
        if (EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO.getNombre().equals(estadoActual) ||
                EstadoPersonal.INACTIVO_POR_RENUNCIA.getNombre().equals(estadoActual)) {
            reactivarPersonal(personalExistente);
        }

        log.info("Personal actualizado exitosamente: {}", personalExistente.getId());

        return mapToCompletoDTO(personalExistente);
    }

    // Métodos de apoyo
    private String generarCodigoVerificacion() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000);
        return String.valueOf(codigo);
    }

    private String generarTokenPersonal() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private Qr generarQrParaPersonal(String carnetIdentidad) {
        QrGenerarDTO qrGenerarDTO = QrGenerarDTO.builder()
                .carnetIdentidad(carnetIdentidad)
                .tipo(TipoQr.PERSONAL)
                .build();

        QrResponseDTO qrResponse = qrService.generarQrPersonal(qrGenerarDTO);
        return qrRepository.findByCodigo(qrResponse.getCodigo())
                .orElseThrow(() -> new BusinessException("Error al generar QR"));
    }

    private void registrarEstadoInicial(Personal personal) {
        Estado estadoRegistrado = estadoRepository.findByEnum(EstadoPersonal.PERSONAL_REGISTRADO)
                .orElseThrow(() -> new BusinessException("Estado PERSONAL REGISTRADO no configurado"));

        EstadoActual estadoActual = EstadoActual.builder()
                .personal(personal)
                .estado(estadoRegistrado)
                .valor_estado_actual(true)
                .build();

        estadoActualRepository.save(estadoActual);
    }

    private void registrarCargoPlanta(Personal personal, Long cargoId) {
        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado con ID: " + cargoId));

        HistorialCargo historial = HistorialCargo.builder()
                .personal(personal)
                .cargo(cargo)
                .fechaInicio(LocalDateTime.now())
                .activo(true)
                .build();

        historialCargoRepository.save(historial);
        log.info("Cargo PLANTA registrado: {} para personal {}", cargo.getNombre(), personal.getId());
    }

    private void registrarCargoEventual(Personal personal, Long cargoProcesoId) {
        CargoProceso cargoProceso = cargoProcesoRepository.findById(cargoProcesoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo de proceso no encontrado con ID: " + cargoProcesoId));

        HistorialCargoProceso historial = HistorialCargoProceso.builder()
                .personal(personal)
                .cargoProceso(cargoProceso)
                .fechaInicio(LocalDateTime.now())
                .activo(true)
                .build();

        historialCargoProcesoRepository.save(historial);
        log.info("Cargo EVENTUAL registrado: {} para personal {}", cargoProceso.getNombre(), personal.getId());
    }

    private void reactivarPersonal(Personal personal) {
        estadoActualRepository.findCurrentEstadoByPersonalId(personal.getId())
                .ifPresent(ea -> {
                    ea.setValor_estado_actual(false);
                    estadoActualRepository.save(ea);
                });

        registrarEstadoInicial(personal);
    }

    private String obtenerEstadoActual(Long personalId) {
        return estadoActualRepository.findCurrentEstadoByPersonalId(personalId)
                .map(ea -> ea.getEstado().getNombre())
                .orElse("SIN ESTADO");
    }

    @Override
    public boolean existeCorreoActivo(String correo) {
        return personalRepository.findByCorreo(correo)
                .map(personal -> {
                    String estado = obtenerEstadoActual(personal.getId());
                    return !estado.equals(EstadoPersonal.CREDENCIAL_DEVUELTO.getNombre()) &&
                            !estado.equals(EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO.getNombre()) &&
                            !estado.equals(EstadoPersonal.INACTIVO_POR_RENUNCIA.getNombre());
                })
                .orElse(false);
    }

    @Override
    public boolean puedeRegistrarseNuevamente(String carnetIdentidad) {
        return personalRepository.findByCarnetIdentidad(carnetIdentidad)
                .map(personal -> {
                    String estado = obtenerEstadoActual(personal.getId());
                    return estado.equals(EstadoPersonal.PERSONAL_REGISTRADO.getNombre()) ||
                            estado.equals(EstadoPersonal.CREDENCIAL_IMPRESO.getNombre()) ||
                            estado.equals(EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO.getNombre()) ||
                            estado.equals(EstadoPersonal.INACTIVO_POR_RENUNCIA.getNombre());
                })
                .orElse(true);
    }
    @Override
    public PersonalDetallesDTO obtenernPersonalQr(String codigQr){
        String carnet= codigQr.split("-")[1];

        Optional<Personal> oPpersonal = personalRepository.findByCarnetIdentidad(carnet);
        /* Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + id)); */
        // solo tipo eventual
        if(oPpersonal.isPresent()){
                Personal personal=oPpersonal.get();
                if(!personal.getAccesoComputo()){
                        throw new BusinessException(
                    "No tiene Acceso a Computo");
                }
                List<HistorialCargoProceso> listaCargo = historialCargoProcesoRepository
                .findByPersonalIdAndActivoTrue(personal.getId());
        CargoProceso cargoProceso = listaCargo.isEmpty() ? null : listaCargo.get(0).getCargoProceso();
        Unidad unidad = cargoProceso != null ? cargoProceso.getUnidad() : null;

        String nombreUnidad = unidad != null ? unidad.getNombre() : null;
        String nombreCargo = cargoProceso != null ? cargoProceso.getNombre() : null;
        String urlImagen = baseUrl + "/api/imagenes/" + personal.getImagen().getIdImagen() + "/descargar";
        String urlQr = baseUrl + "/api/qr/" + personal.getQr().getId() + "/ver";
        return PersonalDetallesDTO.builder()
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
                .estadoActual(obtenerEstadoActual(personal.getId()))
                .createdAt(personal.getCreatedAt())
                .cargo(nombreCargo)
                .unidad(nombreUnidad)
                .imagenId(personal.getImagen() != null ? personal.getImagen().getIdImagen() : null)
                .qrId(personal.getQr() != null ? personal.getQr().getId() : null)
                .imagen(urlImagen)
                .qr(urlQr)
                .build();
        }else{
                throw new BusinessException(
                    "No existe esa persona " +
                            "");
        }
        
    }
    @Override
    public String obtenerMensajeEstadoActual(String carnetIdentidad) {
        return personalRepository.findByCarnetIdentidad(carnetIdentidad)
                .map(personal -> {
                    String estado = obtenerEstadoActual(personal.getId());

                    if (estado.equals(EstadoPersonal.CREDENCIAL_ENTREGADO.getNombre()) ||
                            estado.equals(EstadoPersonal.PERSONAL_ACTIVO.getNombre()) ||
                            estado.equals(EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO.getNombre())) {
                        return "El personal tiene la credencial entregada y activa. " +
                                "Debe devolver la credencial antes de poder registrarse nuevamente.";
                    }

                    if (estado.equals(EstadoPersonal.PERSONAL_REGISTRADO.getNombre()) ||
                            estado.equals(EstadoPersonal.CREDENCIAL_IMPRESO.getNombre())) {
                        return "El personal ya existe y puede actualizar sus datos.";
                    }

                    if (estado.equals(EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO.getNombre()) ||
                            estado.equals(EstadoPersonal.INACTIVO_POR_RENUNCIA.getNombre())) {
                        return "El personal estaba inactivo y puede volver a registrarse.";
                    }

                    return null;
                })
                .orElse(null);
    }

    @Override
    public PersonalCompletoDTO obtenerPersonalPorId(Long id) {
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + id));
        return mapToCompletoDTO(personal);
    }

    @Override
    public PersonalCompletoDTO obtenerPersonalPorCarnet(String carnetIdentidad) {
        Personal personal = personalRepository.findByCarnetIdentidad(carnetIdentidad)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con CI: " + carnetIdentidad));
        return mapToCompletoDTO(personal);
    }

    @Override
    public PersonalCompletoDTO obtenerPersonalPorCorreo(String correo) {
        Personal personal = personalRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con correo: " + correo));
        return mapToCompletoDTO(personal);
    }

    @Override
    public List<PersonalCompletoDTO> listarTodos() {
        return personalRepository.findAll().stream()
                .map(this::mapToCompletoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PersonalCompletoDTO> listarPorTipo(String tipo) {
        TipoPersonal tipoPersonal = TipoPersonal.valueOf(tipo.toUpperCase());
        return personalRepository.findByTipo(tipoPersonal).stream()
                .map(this::mapToCompletoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PersonalCompletoDTO> listarPorEstado(EstadoPersonal estado) {
        return personalRepository.findAllByCurrentEstado(estado.getNombre()).stream()
                .map(this::mapToCompletoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PersonalCompletoDTO actualizarPersonal(Long id, PersonalCreateDTO actualizacionDTO) {
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + id));

        personal.setNombre(actualizacionDTO.getNombre());
        personal.setApellidoPaterno(actualizacionDTO.getApellidoPaterno());
        personal.setApellidoMaterno(actualizacionDTO.getApellidoMaterno());
        personal.setCorreo(actualizacionDTO.getCorreo());
        personal.setCelular(actualizacionDTO.getCelular());
        personal.setAccesoComputo(
                actualizacionDTO.getAccesoComputo() != null ? actualizacionDTO.getAccesoComputo() : false);
        personal.setNroCircunscripcion(actualizacionDTO.getNroCircunscripcion());

        if (actualizacionDTO.getImagenId() != null) {
            Imagen nuevaImagen = imagenService.findEntityById(actualizacionDTO.getImagenId());
            personal.setImagen(nuevaImagen);
        }

        personal = personalRepository.save(personal);

        return mapToCompletoDTO(personal);
    }

    @Override
    public List<PersonalCompletoDTO> buscarPorNombre(String nombre) {
        return personalRepository.buscarPorNombre(nombre).stream()
                .map(this::mapToCompletoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PersonalCompletoDTO actualizarPersonalExistenteAdmin(Long id, PersonalActualizacionDTO actualizacionDTO) {
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + id));

        String estadoActual = obtenerEstadoActual(personal.getId());

        if (EstadoPersonal.CREDENCIAL_ENTREGADO.getNombre().equals(estadoActual) ||
                EstadoPersonal.PERSONAL_ACTIVO.getNombre().equals(estadoActual) ||
                EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO.getNombre().equals(estadoActual)) {
            throw new BusinessException(
                    "El personal tiene la credencial entregada y activa. " +
                            "Debe devolver la credencial antes de editar datos del personal.");
        }

        if (!personal.getCarnetIdentidad().equals(actualizacionDTO.getCarnetIdentidad()) ||
                personal.getQr() == null ||
                personal.getQr().getEstado() == EstadoQr.INACTIVO) {

            // Si cambió el carnet, desactivar QR anterior
            if (!personal.getCarnetIdentidad().equals(actualizacionDTO.getCarnetIdentidad()) &&
                    personal.getQr() != null) {
                personal.getQr().setEstado(EstadoQr.INACTIVO);
                qrRepository.save(personal.getQr());
            }

            Qr nuevoQr = generarQrParaPersonal(actualizacionDTO.getCarnetIdentidad());
            nuevoQr.setPersonal(personal);
            nuevoQr.setEstado(EstadoQr.ASIGNADO);
            qrRepository.save(nuevoQr);
            personal.setQr(nuevoQr);
        }

        personal.setNombre(actualizacionDTO.getNombre());
        personal.setApellidoPaterno(actualizacionDTO.getApellidoPaterno());
        personal.setApellidoMaterno(actualizacionDTO.getApellidoMaterno());
        personal.setCarnetIdentidad(actualizacionDTO.getCarnetIdentidad());
        personal.setCorreo(actualizacionDTO.getCorreo());
        personal.setCelular(actualizacionDTO.getCelular());
        personal.setAccesoComputo(
                actualizacionDTO.getAccesoComputo() != null ? actualizacionDTO.getAccesoComputo() : false);
        personal.setNroCircunscripcion(actualizacionDTO.getNroCircunscripcion());

        if (actualizacionDTO.getImagenId() != null) {
            Imagen nuevaImagen = imagenService.findEntityById(actualizacionDTO.getImagenId());
            personal.setImagen(nuevaImagen);
        }

        personal = personalRepository.save(personal);

        return mapToCompletoDTO(personal);
    }

    @Override
    public ApiResponseDTO deletePersonal(Long id) {
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + id));

        String estadoActual = obtenerEstadoActual(personal.getId());
        if (EstadoPersonal.CREDENCIAL_ENTREGADO.getNombre().equals(estadoActual) ||
                EstadoPersonal.PERSONAL_ACTIVO.getNombre().equals(estadoActual) ||
                EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO.getNombre().equals(estadoActual) ||
                EstadoPersonal.CREDENCIAL_DEVUELTO.getNombre().equals(estadoActual) ||
                EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO.getNombre().equals(estadoActual) ||
                EstadoPersonal.INACTIVO_POR_RENUNCIA.getNombre().equals(estadoActual)) {
            throw new BusinessException(
                    "El personal no puede ser eliminado debido a su estado actual. Debe estar en estado PERSONAL REGISTRADO o CREDENCIAL IMPRESO para ser eliminado.");
        }

        personalRepository.delete(personal);

        ApiResponseDTO response = ApiResponseDTO.builder()
                .success(true)
                .message("Personal eliminado exitosamente con CI: " + personal.getCarnetIdentidad())
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return response;
    }

    @Override
    public PersonalDetallesDTO obtenerDetallesPersonal(Long id) {
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + id));
        // solo tipo eventual
        List<HistorialCargoProceso> listaCargo = historialCargoProcesoRepository
                .findByPersonalIdAndActivoTrue(personal.getId());
        CargoProceso cargoProceso = listaCargo.isEmpty() ? null : listaCargo.get(0).getCargoProceso();
        Unidad unidad = cargoProceso != null ? cargoProceso.getUnidad() : null;

        String nombreUnidad = unidad != null ? unidad.getNombre() : null;
        String nombreCargo = cargoProceso != null ? cargoProceso.getNombre() : null;
        String urlImagen = baseUrl + "/api/imagenes/" + personal.getImagen().getIdImagen() + "/descargar";
        String urlQr = baseUrl + "/api/qr/" + personal.getQr().getId() + "/ver";

        return PersonalDetallesDTO.builder()
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
                .estadoActual(obtenerEstadoActual(personal.getId()))
                .createdAt(personal.getCreatedAt())
                .cargo(nombreCargo)
                .unidad(nombreUnidad)
                .imagenId(personal.getImagen() != null ? personal.getImagen().getIdImagen() : null)
                .qrId(personal.getQr() != null ? personal.getQr().getId() : null)
                .imagen(urlImagen)
                .qr(urlQr)
                .build();
    }

    @Override
    public List<PersonalDetallesDTO> obtenerDetallesPersonal() {
        List<Personal> listaPersonal = personalRepository.findAll();

        return listaPersonal.stream()
                .map(this::mapearAPersonalDetallesDTO)
                .collect(Collectors.toList());
    }

    private PersonalDetallesDTO mapearAPersonalDetallesDTO(Personal personal) {
        // Solo tipo eventual (como en tu código original)
        List<HistorialCargoProceso> listaCargo = historialCargoProcesoRepository
                .findByPersonalIdAndActivoTrue(personal.getId());

        CargoProceso cargoProceso = listaCargo.isEmpty() ? null : listaCargo.get(0).getCargoProceso();
        Unidad unidad = cargoProceso != null ? cargoProceso.getUnidad() : null;

        String nombreUnidad = unidad != null ? unidad.getNombre() : null;
        String nombreCargo = cargoProceso != null ? cargoProceso.getNombre() : null;

        // Construir URLs (manejando nulls)
        String urlImagen = personal.getImagen() != null
                ? baseUrl + "/api/imagenes/" + personal.getImagen().getIdImagen() + "/descargar"
                : null;

        String urlQr = personal.getQr() != null
                ? baseUrl + "/api/qr/" + personal.getQr().getId() + "/ver"
                : null;

        return PersonalDetallesDTO.builder()
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
                .estadoActual(obtenerEstadoActual(personal.getId()))
                .createdAt(personal.getCreatedAt())
                .cargo(nombreCargo)
                .unidad(nombreUnidad)
                .imagenId(personal.getImagen() != null ? personal.getImagen().getIdImagen() : null)
                .qrId(personal.getQr() != null ? personal.getQr().getId() : null)
                .imagen(urlImagen)
                .qr(urlQr)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PersonalDetallesDTO> listarPersonalPorEstado(EstadoPersonal estado) {
        log.info("Listando personales por estado: {}", estado.getNombre());

        // 1. Obtener personales con ese estado (usando tu método existente en el repo)
        List<Personal> listaPersonal = personalRepository.findAllByCurrentEstado(estado.getNombre());

        if (listaPersonal.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Extraer todos los IDs
        List<Long> personalIds = listaPersonal.stream()
                .map(Personal::getId)
                .collect(Collectors.toList());

        // 3. Obtener TODOS los historiales activos de UNA SOLA VEZ
        List<HistorialCargoProceso> historialesActivos = historialCargoProcesoRepository
                .findByPersonalIdInAndActivoTrue(personalIds);

        // 4. Crear mapa para acceso rápido
        Map<Long, HistorialCargoProceso> historialMap = historialesActivos.stream()
                .collect(Collectors.toMap(
                        h -> h.getPersonal().getId(),
                        Function.identity(),
                        (h1, h2) -> h1));

        // 5. Mapear usando el mapa
        return listaPersonal.stream()
                .map(personal -> mapearAPersonalDetallesDTOConMapa(personal, historialMap))
                .collect(Collectors.toList());
    }

    @Override
    public PersonalDetallesDTO verificarAcceso (String qrCodigo) {
        return null;
    }

    @Override
    public ApiResponseDTO cambiarEstadoAcceso(Long id) {
        Personal personal = personalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + id));

        boolean nuevoEstado = !Boolean.TRUE.equals(personal.getAccesoComputo());
        personal.setAccesoComputo(nuevoEstado);

        // Guardar cambios
        personalRepository.save(personal);

        return ApiResponseDTO.builder()
                .success(true)
                .message("Estado de acceso a cómputo actualizado correctamente")
                .status(HttpStatus.OK.value())
                .build();
    }

    private PersonalDetallesDTO mapearAPersonalDetallesDTOConMapa(
            Personal personal,
            Map<Long, HistorialCargoProceso> historialMap) {

        HistorialCargoProceso historial = historialMap.get(personal.getId());
        CargoProceso cargoProceso = historial != null ? historial.getCargoProceso() : null;
        Unidad unidad = cargoProceso != null ? cargoProceso.getUnidad() : null;

        String nombreUnidad = unidad != null ? unidad.getNombre() : null;
        String nombreCargo = cargoProceso != null ? cargoProceso.getNombre() : null;

        String urlImagen = personal.getImagen() != null
                ? baseUrl + "/api/imagenes/" + personal.getImagen().getIdImagen() + "/descargar"
                : null;

        String urlQr = personal.getQr() != null
                ? baseUrl + "/api/qr/" + personal.getQr().getId() + "/ver"
                : null;

        return PersonalDetallesDTO.builder()
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
                .estadoActual(obtenerEstadoActual(personal.getId()))
                .createdAt(personal.getCreatedAt())
                .cargo(nombreCargo)
                .unidad(nombreUnidad)
                .imagenId(personal.getImagen() != null ? personal.getImagen().getIdImagen() : null)
                .qrId(personal.getQr() != null ? personal.getQr().getId() : null)
                .imagen(urlImagen)
                .qr(urlQr)
                .build();
    }

    private PersonalCompletoDTO mapToCompletoDTO(Personal personal) {
        String estadoActual = obtenerEstadoActual(personal.getId());

        return PersonalCompletoDTO.builder()
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
                .estadoActual(estadoActual)
                .createdAt(personal.getCreatedAt())
                .imagenId(personal.getImagen() != null ? personal.getImagen().getIdImagen() : null)
                .qrId(personal.getQr() != null ? personal.getQr().getId() : null)
                .build();
    }
}