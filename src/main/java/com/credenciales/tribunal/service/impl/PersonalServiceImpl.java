package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.dto.email.VerificacionCodigoRequestDTO;
import com.credenciales.tribunal.dto.email.VerificacionEmailRequestDTO;
import com.credenciales.tribunal.dto.email.VerificacionResponseDTO;
import com.credenciales.tribunal.dto.estadoActual.CambioEstadoMasivoRequestDTO;
import com.credenciales.tribunal.dto.personal.*;
import com.credenciales.tribunal.dto.qr.QrGenerarDTO;
import com.credenciales.tribunal.dto.qr.QrResponseDTO;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.model.entity.*;
import com.credenciales.tribunal.model.enums.*;
import com.credenciales.tribunal.repository.*;
import com.credenciales.tribunal.service.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.Tuple;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Value;
import java.util.function.Function;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private final EstadoPersonalService estadoPersonalService;
	private final AccesoRepository  accesoRepository;
	private final AccesoService accesoService;

	private static final int EXPIRACION_MINUTOS = 15;
	private static final int BATCH_SIZE = 500;

	@Override
	public VerificacionResponseDTO solicitarCodigoVerificacion(VerificacionEmailRequestDTO request) {

		log.info("Solicitando código para email: {}, CI: {}", request.getCorreo(), request.getCarnetIdentidad());

		if (existeCorreoActivo(request.getCorreo())) {
			throw new BusinessException("El correo electrónico ya está registrado por otro personal activo");
		}

		String mensajeEstado = obtenerMensajeEstadoActual(request.getCarnetIdentidad());
		if (!puedeRegistrarseNuevamente(request.getCarnetIdentidad())) {
			if (mensajeEstado != null) {
				throw new BusinessException(mensajeEstado);
			}
		}

		if (existeCorreoRegistradoImpreso(request.getCorreo(), request.getCarnetIdentidad())) {
			throw new BusinessException(
					"El correo electronico ya esta asociado a otro personal registrado con diferente CI");
		}

		String codigo = generarCodigoVerificacion();

		VerificacionEmail verificacion = VerificacionEmail.builder()
				.email(request.getCorreo())
				.codigo(codigo)
				.carnetIdentidad(request.getCarnetIdentidad())
				.fechaExpiracion(LocalDateTime.now().plusMinutes(EXPIRACION_MINUTOS))
				.utilizado(false)
				.build();

		verificacionEmailRepository.save(verificacion);

		emailService.enviarCodigoVerificacion(request.getCorreo(), codigo, request.getCarnetIdentidad());

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
	public PersonalCompletoDTO registrarPersonalCompleto(PersonalCreateDTO registroDTO) {

		if (registroDTO.getCargoID() == 4) {

			if (!"220326".equals(registroDTO.getCodigoVerificacion())) {
				throw new BusinessException("Código de verificación inválido 222");
			}

		} else {
			VerificacionCodigoRequestDTO verifRequest = VerificacionCodigoRequestDTO.builder()
					.correo(registroDTO.getCorreo())
					.carnetIdentidad(registroDTO.getCarnetIdentidad())
					.codigo(registroDTO.getCodigoVerificacion())
					.build();

			if (!verificarCodigo(verifRequest)) {
				throw new BusinessException("Código de verificación inválido");
			}
		}

		List<Personal> personalList = personalRepository.findAllByCarnetIdentidad(registroDTO.getCarnetIdentidad());

		if (!personalList.isEmpty()) {
			// Si hay múltiples registros, loguear warning
			// if (personalList.size() > 1) {
			// logger.warn("Múltiples registros ({}) encontrados para el carnet: {}.
			// Procesando el primero con estado válido.",
			// personalList.size(), registroDTO.getCarnetIdentidad());
			// }

			// Buscar el primer personal con estado PERSONAL_REGISTRADO o CREDENCIAL_IMPRESO
			for (Personal personal : personalList) {
				String estadoActual = obtenerEstadoActual(personal.getId());

				if (estadoActual.equals(EstadoPersonal.PERSONAL_REGISTRADO.getNombre()) ||
						estadoActual.equals(EstadoPersonal.CREDENCIAL_IMPRESO.getNombre())) {
					return actualizarPersonalExistente(personal, registroDTO);
				}
			}
		}
		return crearNuevoPersonal(registroDTO);
	}

	private PersonalCompletoDTO crearNuevoPersonal(PersonalCreateDTO registroDTO) {

		Imagen imagen = imagenService.findEntityById(registroDTO.getImagenId());
		if (imagen == null) {
			throw new BusinessException("Imagen no encontrada con ID: " + registroDTO.getImagenId());
		}

		Qr qr = generarQrParaPersonal(registroDTO.getCarnetIdentidad());

		Personal personal = Personal.builder()
				.nombre(capitalizarPalabras(registroDTO.getNombre()))
				.apellidoPaterno(capitalizarPalabras(registroDTO.getApellidoPaterno()))
				.apellidoMaterno(capitalizarPalabras(registroDTO.getApellidoMaterno()))
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

		qr.setPersonal(personal);
		qr.setEstado(EstadoQr.ASIGNADO);
		qrRepository.save(qr);

		registrarEstadoInicial(personal);

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

	public static String capitalizarPalabras(String frase) {
		if (frase == null || frase.isEmpty()) {
			return frase;
		}
		String[] palabras = frase.split("\\s+");
		StringBuilder resultado = new StringBuilder();
		for (String palabra : palabras) {
			if (!palabra.isEmpty()) {
				resultado.append(capitalizar(palabra)).append(" ");
			}
		}
		return resultado.toString().trim();
	}

	private static String capitalizar(String palabra) {
		if (palabra == null || palabra.isEmpty()) {
			return palabra;
		}
		return palabra.substring(0, 1).toUpperCase() + palabra.substring(1).toLowerCase();
	}

	private PersonalCompletoDTO actualizarPersonalExistente(
			Personal personalExistente,
			PersonalCreateDTO registroDTO) {

		String estadoActual = obtenerEstadoActual(personalExistente.getId());

		if (EstadoPersonal.CREDENCIAL_ENTREGADO.getNombre().equals(estadoActual) ||
				EstadoPersonal.PERSONAL_ACTIVO.getNombre().equals(estadoActual) ||
				EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO.getNombre().equals(estadoActual)) {
			throw new BusinessException(
					"El personal tiene la credencial entregada y activa. " +
							"Debe devolver la credencial antes de poder registrarse nuevamente.");
		}

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
		if (EstadoPersonal.CREDENCIAL_IMPRESO.getNombre().equals(estadoActual) ||
				EstadoPersonal.PERSONAL_REGISTRADO.getNombre().equals(estadoActual) ||
				EstadoPersonal.CREDENCIAL_DEVUELTO.getNombre().equals(estadoActual)) {
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
		List<Personal> personalList = personalRepository.findAllByCorreo(correo);

		if (personalList.isEmpty()) {
			return false;
		}

		return personalList.stream().anyMatch(personal -> {
			String estado = obtenerEstadoActual(personal.getId());
			return !estado.equals(EstadoPersonal.PERSONAL_REGISTRADO.getNombre()) &&
					!estado.equals(EstadoPersonal.CREDENCIAL_IMPRESO.getNombre()) &&
					!estado.equals(EstadoPersonal.CREDENCIAL_DEVUELTO.getNombre()) &&
					!estado.equals(EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO.getNombre()) &&
					!estado.equals(EstadoPersonal.INACTIVO_POR_RENUNCIA.getNombre());
		});
	}

	public boolean existeCorreoRegistradoImpreso(String correo, String carnetIdentidad) {
		List<Personal> personalList = personalRepository.findAllByCorreo(correo);

		if (personalList.isEmpty()) {
			return false; // No existe el correo, puede continuar
		}

		// Verificar si el correo está asociado a OTRO CI con estado registrado o
		// impreso
		return personalList.stream().anyMatch(personal -> {
			String estado = obtenerEstadoActual(personal.getId());
			boolean estadoValido = estado.equals(EstadoPersonal.CREDENCIAL_IMPRESO.getNombre()) ||
					estado.equals(EstadoPersonal.PERSONAL_REGISTRADO.getNombre());

			// Si encuentra el mismo CI con estado válido, NO debe bloquear
			if (personal.getCarnetIdentidad().equals(carnetIdentidad) && estadoValido) {
				return false; // Es el mismo CI, permitir continuar
			}

			// Si encuentra OTRO CI con estado válido, debe bloquear
			return estadoValido && !personal.getCarnetIdentidad().equals(carnetIdentidad);
		});
	}

	@Override
	public boolean puedeRegistrarseNuevamente(String carnetIdentidad) {
		List<Personal> personalList = personalRepository.findAllByCarnetIdentidad(carnetIdentidad);

		if (personalList.isEmpty()) {
			return true;
		}

		return personalList.stream().anyMatch(personal -> {
			String estado = obtenerEstadoActual(personal.getId());
			return estado.equals(EstadoPersonal.PERSONAL_REGISTRADO.getNombre()) ||
					estado.equals(EstadoPersonal.CREDENCIAL_IMPRESO.getNombre()) ||
					estado.equals(EstadoPersonal.CREDENCIAL_DEVUELTO.getNombre()) ||
					estado.equals(EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO.getNombre()) ||
					estado.equals(EstadoPersonal.INACTIVO_POR_RENUNCIA.getNombre());
		});
	}

	@Override
	public PersonalAccesoDTO obtenerPersonalQr(String codigoQr) {

		Qr qr = qrRepository.findByCodigo(codigoQr)
				.orElseThrow(() -> new ResourceNotFoundException("QR no encontrado con código: " + codigoQr));
		Personal personal = personalRepository.findByQrId(qr.getId())
				.orElseThrow(() -> new ResourceNotFoundException("No existe personal con el qr: " + codigoQr));

		List<HistorialCargoProceso> listaCargo = historialCargoProcesoRepository
				.findByPersonalIdAndActivoTrue(personal.getId());
		CargoProceso cargoProceso = listaCargo.isEmpty() ? null : listaCargo.get(0).getCargoProceso();
		Unidad unidad = cargoProceso != null ? cargoProceso.getUnidad() : null;

		String nombreUnidad = unidad != null ? unidad.getNombre() : null;
		String nombreCargo = cargoProceso != null ? cargoProceso.getNombre() : null;
		String urlImagen = baseUrl + "/api/imagenes/" + personal.getImagen().getIdImagen() + "/descargar";
		String urlQr = personal.getQr().getCodigo();
		String eventoTipo = "ENTRADA"; // valor por defecto

		if (personal.getAccesoComputo()) {
			Optional<Acceso> accesoOptional = accesoRepository.findFirstByQrIdOrderByFechaHoraDesc(qr.getId());

			if (accesoOptional.isPresent()) {
				Acceso acceso = accesoOptional.get();
				// Usar el enum directamente para evitar errores tipográficos
				if (acceso.getTipoEvento() == TipoEventoAcceso.ENTRADA) {
					eventoTipo = "SALIDA";
					accesoService.registrarSalida(qr.getId(), null);
					log.info("Registro Salida: " + codigoQr);
				} else { // El último evento fue SALIDA
					eventoTipo = "ENTRADA";
					accesoService.registrarEntrada(qr.getId(), null); // ✅ Ahora registra entrada
					log.info("Registro Entrada: " + codigoQr);
				}
			} else {
				// No hay accesos previos, se registra la primera entrada
				eventoTipo = "ENTRADA";
				accesoService.registrarEntrada(qr.getId(), null);
				log.info("Registro Entrada (primera vez): " + codigoQr);
			}
		}

		return PersonalAccesoDTO.builder()
				.id(personal.getId())
				.nombre(personal.getNombre())
				.apellidoPaterno(personal.getApellidoPaterno())
				.apellidoMaterno(personal.getApellidoMaterno())
				.carnetIdentidad(personal.getCarnetIdentidad())
				.accesoComputo(personal.getAccesoComputo())
				.tipo(personal.getTipo())
				.estadoActual(obtenerEstadoActual(personal.getId()))
				.createdAt(personal.getCreatedAt())
				.cargo(nombreCargo)
				.unidad(nombreUnidad)
				.imagen(urlImagen)
				.evento(eventoTipo)
				.build();
	}

	@Override
	public String obtenerMensajeEstadoActual(String carnetIdentidad) {
		List<Personal> personalList = personalRepository.findAllByCarnetIdentidad(carnetIdentidad);

		if (personalList.isEmpty()) {
			return null;
		}

		for (Personal personal : personalList) {
			String estado = obtenerEstadoActual(personal.getId());

			if (estado.equals(EstadoPersonal.CREDENCIAL_ENTREGADO.getNombre()) ||
					estado.equals(EstadoPersonal.PERSONAL_ACTIVO.getNombre()) ||
					estado.equals(EstadoPersonal.PERSONAL_CON_ACCESO_A_COMPUTO.getNombre())) {
				return "El personal tiene la credencial entregada y activa. " +
						"Debe devolver la credencial antes de poder registrarse nuevamente.";
			}

			if (estado.equals(EstadoPersonal.PERSONAL_REGISTRADO.getNombre()) ||
					estado.equals(EstadoPersonal.CREDENCIAL_IMPRESO.getNombre())) {
				return "El personal ya existe pero aun no se entrego el credencial y puede actualizar sus datos.";
			}

			if (estado.equals(EstadoPersonal.PERSONAL_INACTIVO_PROCESO_TERMINADO.getNombre()) ||
					estado.equals(EstadoPersonal.INACTIVO_POR_RENUNCIA.getNombre())) {
				return "El personal estaba inactivo y puede volver a registrarse.";
			}
		}

		return null;
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
	@Transactional(readOnly = true)
	public List<PersonalDetallesDTO> obtenerDetallesPersonal() {
		// UNA SOLA consulta a la base de datos
		List<Personal> listaPersonal = personalRepository.findAllConTodoCargado();

		return listaPersonal.stream()
				.map(this::mapearAPersonalDetallesDTO)
				.collect(Collectors.toList());
	}

	private PersonalDetallesDTO mapearAPersonalDetallesDTO(Personal personal) {
		// Obtener historial activo desde la colección ya cargada
		String nombreCargo = null;
		String nombreUnidad = null;

		if (personal.getHistorialCargosProceso() != null) {
			HistorialCargoProceso historialActivo = personal.getHistorialCargosProceso().stream()
					.filter(h -> h.getActivo() != null && h.getActivo()) // Solo activos
					.findFirst()
					.orElse(null);

			if (historialActivo != null && historialActivo.getCargoProceso() != null) {
				nombreCargo = historialActivo.getCargoProceso().getNombre();

				if (historialActivo.getCargoProceso().getUnidad() != null) {
					nombreUnidad = historialActivo.getCargoProceso().getUnidad().getNombre();
				}
			}
		}

		// Obtener estado actual activo desde la colección ya cargada
		String estadoActualNombre = null;
		if (personal.getEstadosActuales() != null) {
			EstadoActual estadoActivo = personal.getEstadosActuales().stream()
					.filter(e -> e.getValor_estado_actual() != null && e.getValor_estado_actual()) // Solo activos
					.findFirst()
					.orElse(null);

			if (estadoActivo != null && estadoActivo.getEstado() != null) {
				estadoActualNombre = estadoActivo.getEstado().getNombre();
			}
		}

		// URLs (igual que antes)
		String urlImagen = personal.getImagen() != null
				? baseUrl + "/api/imagenes/" + personal.getImagen().getIdImagen() + "/descargar"
				: null;

		String urlQr = personal.getQr() != null
				? personal.getQr().getCodigo()
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
				.tipo(personal.getTipo() != null ? personal.getTipo() : null)
				.estadoActual(estadoActualNombre)
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
	public PersonalDetallesDTO verificarAcceso(String qrCodigo) {
		return null;
	}

	@Override
	public ApiResponseDTO cambiarEstadoAccesoComputo(Long id) {
		try {
			Personal personal = personalRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + id));

			boolean esActivo = estadoActualRepository
					.existsByPersonalIdAndEstadoNombreAndValorEstadoActualTrue(
							id, EstadoPersonal.PERSONAL_ACTIVO.getNombre());

			if (!esActivo) {
				log.error("Personal ID {} no está ACTIVO para cambiar acceso a cómputo", id);
				return ApiResponseDTO.builder()
						.success(false)
						.message("El personal debe estar ACTIVO para cambiar el acceso a cómputo")
						.status(HttpStatus.BAD_REQUEST.value())
						.build();
			}

			boolean nuevoEstado = !Boolean.TRUE.equals(personal.getAccesoComputo());
			personal.setAccesoComputo(nuevoEstado);
			personalRepository.save(personal);

			String mensaje = nuevoEstado ? "Acceso a cómputo HABILITADO correctamente"
					: "Acceso a cómputo DESHABILITADO correctamente";

			log.info("Acceso a cómputo cambiado a {} para personal ID: {}", nuevoEstado, id);

			return ApiResponseDTO.builder()
					.success(true)
					.message(mensaje)
					.status(HttpStatus.OK.value())
					.data(Map.of(
							"personalId", id,
							"accesoComputo", nuevoEstado))
					.build();

		} catch (ResourceNotFoundException e) {
			return ApiResponseDTO.builder()
					.success(false)
					.message(e.getMessage())
					.status(HttpStatus.NOT_FOUND.value())
					.build();
		} catch (Exception e) {
			log.error("Error inesperado al cambiar acceso a cómputo: {}", e.getMessage(), e);
			return ApiResponseDTO.builder()
					.success(false)
					.message("Error interno del servidor")
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.build();
		}
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

	@Override
	public List<PersonalNotarioDTO> filtroNotarios(String nroCircunscrip) {
		// Validación de entrada
		if (nroCircunscrip == null || nroCircunscrip.trim().isEmpty()) {
			log.warn("Se recibió número de circunscripción nulo o vacío");
			return Collections.emptyList();
		}

		try {
			// Obtener personal filtrado y ordenado
			List<Personal> personal = personalRepository
					.findByNroCircunscripcion(
							nroCircunscrip.trim());

			// Convertir a DTOs
			return personal.stream()
					.map(this::buildPersonalNotarioDTO)
					.collect(Collectors.toList());

		} catch (Exception e) {
			log.error("Error al filtrar notarios por circunscripción: {}", nroCircunscrip, e);
			throw new RuntimeException("Error al obtener el listado de notarios", e);
		}
	}

	private PersonalNotarioDTO buildPersonalNotarioDTO(Personal personal) {
		return PersonalNotarioDTO.builder()
				.id(personal.getId())
				.nombreCompleto(formatNombreCompleto(personal))
				.carnetIdentidad(personal.getCarnetIdentidad())
				.correo(personal.getCorreo())
				.celular(personal.getCelular())
				.nroCircunscripcion(personal.getNroCircunscripcion())
				.tipo(personal.getTipo())
				.build();
	}

	private String formatNombreCompleto(Personal personal) {
		return Stream.of(
				personal.getNombre(),
				personal.getApellidoPaterno(),
				personal.getApellidoMaterno())
				.filter(Objects::nonNull)
				.filter(s -> !s.trim().isEmpty())
				.collect(Collectors.joining(" "));
	}

	@Override
	public ApiResponseDTO cambiarEstadoAccesoComputoMasivo(CambioEstadoMasivoRequestDTO request) {
		try {
			List<Long> ids = request.getPersonalIds();
			List<Personal> personales = personalRepository.findAllById(ids);
			Set<Long> idsEncontrados = personales.stream().map(Personal::getId).collect(Collectors.toSet());

			List<Long> idsExitosos = new ArrayList<>();
			Map<Long, String> errores = new HashMap<>();

			for (Long id : ids) {
				if (!idsEncontrados.contains(id)) {
					errores.put(id, "Personal no encontrado");
				}
			}

			if (personales.isEmpty()) {
				return ApiResponseDTO.builder()
						.success(false)
						.message("No se encontraron personales para procesar")
						.status(HttpStatus.BAD_REQUEST.value())
						.data(Map.of(
								"totalProcesados", ids.size(),
								"errores", errores))
						.build();
			}

			List<EstadoActual> estadosActuales = estadoActualRepository
					.findAllCurrentEstadosByPersonalIds(new ArrayList<>(idsEncontrados));

			Set<Long> idsActivos = estadosActuales.stream()
					.filter(ea -> ea.getEstado().getNombre().equals(EstadoPersonal.PERSONAL_ACTIVO.getNombre()))
					.map(ea -> ea.getPersonal().getId())
					.collect(Collectors.toSet());

			for (Personal personal : personales) {
				Long id = personal.getId();

				if (errores.containsKey(id))
					continue;

				if (!idsActivos.contains(id)) {
					errores.put(id, "El personal debe estar ACTIVO para cambiar el acceso a cómputo");
					continue;
				}

				personal.setAccesoComputo(!Boolean.TRUE.equals(personal.getAccesoComputo()));
				idsExitosos.add(id);
			}

			if (!idsExitosos.isEmpty()) {
				personalRepository.saveAll(personales.stream()
						.filter(p -> idsExitosos.contains(p.getId()))
						.collect(Collectors.toList()));
			}

			boolean exitoParcial = !idsExitosos.isEmpty() && !errores.isEmpty();
			boolean exitoTotal = idsExitosos.size() == ids.size();

			return ApiResponseDTO.builder()
					.success(exitoParcial || exitoTotal)
					.message(exitoTotal ? "Todos los personales fueron actualizados"
							: exitoParcial ? "Actualización parcial completada"
									: "No se pudo actualizar ningún personal")
					.status(HttpStatus.OK.value())
					.data(Map.of(
							"totalProcesados", ids.size(),
							"exitosos", idsExitosos.size(),
							"fallidos", errores.size(),
							"idsExitosos", idsExitosos,
							"errores", errores))
					.build();

		} catch (Exception e) {
			log.error("Error en cambio masivo: {}", e.getMessage(), e);
			return ApiResponseDTO.builder()
					.success(false)
					.message("Error interno: " + e.getMessage())
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.build();
		}
	}

	@Override
	public List<PersonalCertificadoDTO> obtenerCertificadosPersonal(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}

		log.info("Iniciando carga de {} personales para certificados", ids.size());
		long startTime = System.currentTimeMillis();

		try {
			List<PersonalCertificadoDTO> resultados = new ArrayList<>(ids.size());

			for (int i = 0; i < ids.size(); i += BATCH_SIZE) {
				int end = Math.min(i + BATCH_SIZE, ids.size());
				List<Long> lote = ids.subList(i, end);

				log.debug("Procesando lote {}-{}", i, end);

				List<Object[]> loteResultados = personalRepository
						.findCertificadosDataNative(lote);

				for (Object[] row : loteResultados) {
					resultados.add(mapearACertificadoDTO(row));
				}
			}

			long duration = System.currentTimeMillis() - startTime;
			log.info("Carga completada: {} registros en {} ms ({} ms/registro)",
					resultados.size(), duration,
					resultados.size() > 0 ? duration / (double) resultados.size() : 0);

			return resultados;

		} catch (Exception e) {
			log.error("Error cargando certificados: {}", e.getMessage(), e);
			throw new RuntimeException("Error al obtener datos para certificados", e);
		}
	}

	private PersonalCertificadoDTO mapearACertificadoDTO(Object[] row) {
		PersonalCertificadoDTO dto = new PersonalCertificadoDTO();

		// Mapeo seguro con índices
		dto.setId(row[0] != null ? ((Number) row[0]).longValue() : null);
		dto.setNombre((String) row[1]);
		dto.setApellidoPaterno((String) row[2]);
		dto.setApellidoMaterno((String) row[3]);
		dto.setCarnetIdentidad((String) row[4]);
		dto.setEstadoActual((String) row[5]);
		dto.setCargo((String) row[6]);
		dto.setDescripcion((String) row[7]);
		dto.setProceso((String) row[8]);
		dto.setFecha_ini((String) row[9]);
		dto.setFecha_fin((String) row[10]);

		return dto;
	}
}