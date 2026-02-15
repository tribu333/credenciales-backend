package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.personal.RegistroRequestDTO;
import com.credenciales.tribunal.dto.personal.VerificacionCorreoDTO;
import com.credenciales.tribunal.model.entity.*;
import com.credenciales.tribunal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class RegistroService {

    private final PersonalRepository personalRepository;
    private final EstadoRepository estadoRepository;
    private final EstadoActualRepository estadoActualRepository;
    private final EmailService emailService;

    @Transactional
    public void solicitarRegistro(RegistroRequestDTO dto) {

        var existente = personalRepository
                .findTopByCarnetIdentidadOrderByCreatedAtDesc(dto.getCarnetIdentidad());

        if (existente.isPresent()) {

            String estado = obtenerUltimoEstado(existente.get());

            if ("CREDENCIAL_ENTREGADA".equals(estado)) {
                throw new RuntimeException("Debe devolver la credencial.");
            }
        }

        String codigo = generarCodigo();

        Personal personal = Personal.builder()
                .nombre(dto.getNombre())
                .apellidoPaterno(dto.getApellidoPaterno())
                .apellidoMaterno(dto.getApellidoMaterno())
                .carnetIdentidad(dto.getCarnetIdentidad())
                .correo(dto.getCorreo())
                .celular(dto.getCelular())
                .codigoVerificacion(codigo)
                .codigoExpiracion(LocalDateTime.now().plusMinutes(10))
                .verificado(false)
                .build();

        personalRepository.save(personal);

        cambiarEstado(personal, "REGISTRADO");

        emailService.enviarCodigo(dto.getCorreo(), codigo);
    }

    @Transactional
    public void verificarCodigo(VerificacionCorreoDTO request) {

        Personal personal = personalRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("No encontrado"));

        if (personal.getCodigoExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado");
        }

        if (!personal.getCodigoVerificacion().equals(request.getCodigo())) {
            throw new RuntimeException("Código incorrecto");
        }

        personal.setVerificado(true);
        personal.setCodigoVerificacion(null);

        personalRepository.save(personal);

        cambiarEstado(personal, "VERIFICADO");
    }

    private void cambiarEstado(Personal personal, String nombreEstado) {

        Estado estado = estadoRepository.findByNombreEstado(nombreEstado)
                .orElseThrow();

        EstadoActual nuevo = new EstadoActual();
        nuevo.setPersonal(personal);
        nuevo.setEstado(estado);
        nuevo.setFecha(LocalDateTime.now());

        estadoActualRepository.save(nuevo);
    }

    private String obtenerUltimoEstado(Personal personal) {
    return personal.getEstadosActuales()
            .stream()
            .max(Comparator.comparing(EstadoActual::getFecha))
            .map(e -> e.getEstado().getNombreEstado())
            .orElse("SIN_ESTADO");
    }


    private String generarCodigo() {
        return String.valueOf((int)((Math.random() * 900000) + 100000));
    }
}
