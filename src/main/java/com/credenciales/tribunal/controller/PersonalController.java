package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.email.VerificacionCodigoRequestDTO;
import com.credenciales.tribunal.dto.email.VerificacionEmailRequestDTO;
import com.credenciales.tribunal.dto.email.VerificacionResponseDTO;
import com.credenciales.tribunal.dto.personal.*;
import com.credenciales.tribunal.model.enums.EstadoPersonal;
import com.credenciales.tribunal.service.PersonalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Personal", description = "API para la gestión completa de personal")
public class PersonalController {

    private final PersonalService personalService;

    @Operation(summary = "Solicitar código de verificación", description = "Envía un código de verificación de 6 dígitos al correo electrónico")
    @PostMapping("/solicitar-codigo")
    public ResponseEntity<VerificacionResponseDTO> solicitarCodigoVerificacion(
            @Valid @RequestBody VerificacionEmailRequestDTO request) {
        return ResponseEntity.ok(personalService.solicitarCodigoVerificacion(request));
    }

    @Operation(summary = "Verificar código", description = "Verifica que el código ingresado sea correcto y no haya expirado")
    @PostMapping("/verificar-codigo")
    public ResponseEntity<Boolean> verificarCodigo(
            @Valid @RequestBody VerificacionCodigoRequestDTO request) {
        return ResponseEntity.ok(personalService.verificarCodigo(request));
    }

    @Operation(summary = "Registrar personal completo", description = "Registra un nuevo personal con ID de imagen existente")
    @PostMapping("/registrar")
    public ResponseEntity<PersonalCompletoDTO> registrarPersonal(
            @Valid @RequestBody PersonalCreateDTO registroDTO) {
        return new ResponseEntity<>(
                personalService.registrarPersonalCompleto(registroDTO),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Verificar si puede registrarse nuevamente", description = "Verifica si un CI puede ser usado para un nuevo registro")
    @GetMapping("/puede-registrarse/{carnetIdentidad}")
    public ResponseEntity<Boolean> puedeRegistrarseNuevamente(
            @PathVariable String carnetIdentidad) {
        return ResponseEntity.ok(personalService.puedeRegistrarseNuevamente(carnetIdentidad));
    }

    @Operation(summary = "Obtener mensaje de estado", description = "Obtiene un mensaje descriptivo del estado actual del personal")
    @GetMapping("/mensaje-estado/{carnetIdentidad}")
    public ResponseEntity<String> obtenerMensajeEstado(
            @PathVariable String carnetIdentidad) {
        String mensaje = personalService.obtenerMensajeEstadoActual(carnetIdentidad);
        return mensaje != null ? ResponseEntity.ok(mensaje) : ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener personal por ID", description = "Retorna la información completa de un personal por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<PersonalCompletoDTO> obtenerPersonalPorId(
            @Parameter(description = "ID del personal", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(personalService.obtenerPersonalPorId(id));
    }

    @Operation(summary = "Obtener personal por CI", description = "Retorna la información completa de un personal por su carnet de identidad")
    @GetMapping("/ci/{carnetIdentidad}")
    public ResponseEntity<PersonalCompletoDTO> obtenerPersonalPorCarnet(
            @Parameter(description = "Carnet de identidad", example = "1234567") @PathVariable String carnetIdentidad) {
        return ResponseEntity.ok(personalService.obtenerPersonalPorCarnet(carnetIdentidad));
    }

    @Operation(summary = "Listar todos", description = "Retorna la lista completa de todos los personales")
    @GetMapping
    public ResponseEntity<List<PersonalCompletoDTO>> listarTodos() {
        return ResponseEntity.ok(personalService.listarTodos());
    }

    @Operation(summary = "Listar por tipo", description = "Retorna personales filtrados por tipo (PLANTA/EVENTUAL)")
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<PersonalCompletoDTO>> listarPorTipo(
            @Parameter(description = "Tipo de personal", example = "PLANTA") @PathVariable String tipo) {
        return ResponseEntity.ok(personalService.listarPorTipo(tipo));
    }

    @Operation(summary = "Listar por estado", description = "Retorna personales filtrados por su estado actual")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PersonalCompletoDTO>> listarPorEstado(
            @Parameter(description = "Estado del personal") @PathVariable EstadoPersonal estado) {
        return ResponseEntity.ok(personalService.listarPorEstado(estado));
    }

    @Operation(summary = "Buscar por nombre", description = "Busca personales por nombre o apellido")
    @GetMapping("/buscar")
    public ResponseEntity<List<PersonalCompletoDTO>> buscarPorNombre(
            @Parameter(description = "Texto a buscar", example = "Juan") @RequestParam String q) {
        return ResponseEntity.ok(personalService.buscarPorNombre(q));
    }

    @Operation(summary = "Actualizar personal", description = "Actualiza los datos de un personal existente")
    @PutMapping("/{id}")
    public ResponseEntity<PersonalCompletoDTO> actualizarPersonal(
            @PathVariable Long id,
            @Valid @RequestBody PersonalCreateDTO actualizacionDTO) { // ← Sin MultipartFile
        return ResponseEntity.ok(
                personalService.actualizarPersonal(id, actualizacionDTO));
    }

    @Operation(summary = "Actualizar personal desde admin", description = "Actualiza los datos de un personal existente")
    @PutMapping("/{id}/admin")
    public ResponseEntity<PersonalCompletoDTO> actualizarPersonalExistenteAdmin(
            @PathVariable Long id,
            @Valid @RequestBody PersonalActualizacionDTO actualizacionDTO) { // ← Sin MultipartFile
        return ResponseEntity.ok(
                personalService.actualizarPersonalExistenteAdmin(id, actualizacionDTO));
    }

    @Operation(summary = "Verificar correo activo", description = "Verifica si un correo ya está siendo usado por un personal activo")
    @GetMapping("/verificar-correo/{correo}")
    public ResponseEntity<Boolean> existeCorreoActivo(
            @Parameter(description = "Correo electrónico", example = "juan@ejemplo.com") @PathVariable String correo) {
        return ResponseEntity.ok(personalService.existeCorreoActivo(correo));
    }

    @Operation(summary = "Eliminar personal", description = "Elimina un personal por su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> eliminarPersonal(
            @Parameter(description = "ID del personal", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(personalService.deletePersonal(id));
    }

    @Operation(summary = "Obtener detalles de personal", description = "Retorna información detallada de un personal, incluyendo su cargo y unidad actual")
    @GetMapping("/{id}/detalles")
    public ResponseEntity<PersonalDetallesDTO> obtenerDetallesPersonal(
            @Parameter(description = "ID del personal", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(personalService.obtenerDetallesPersonal(id));
    }

    @Operation(summary = "Obtener todos los detalles de personal", description = "Retorna información detallada de todos los personales, incluyendo su cargo y unidad actual")
    @GetMapping("/detalles")
    public ResponseEntity<List<PersonalDetallesDTO>> obtenerDetallesPersonal() {
        return ResponseEntity.ok(personalService.obtenerDetallesPersonal());
    }

}
