package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.config.QrStorageProperties;
import com.credenciales.tribunal.dto.qr.QrGenerarDTO;
import com.credenciales.tribunal.dto.qr.QrResponseDTO;
import com.credenciales.tribunal.exception.BusinessException;
import com.credenciales.tribunal.exception.ResourceNotFoundException;
import com.credenciales.tribunal.model.entity.Personal;
import com.credenciales.tribunal.model.entity.Qr;
import com.credenciales.tribunal.model.enums.EstadoQr;
import com.credenciales.tribunal.model.enums.TipoQr;
import com.credenciales.tribunal.repository.PersonalRepository;
import com.credenciales.tribunal.repository.QrRepository;
import com.credenciales.tribunal.service.QrService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QrServiceImpl implements QrService {

    private final QrRepository qrRepository;
    private final PersonalRepository personalRepository;
    private final QrStorageProperties storageProperties;
    
    @Value("${qr.base-url}")
    private String baseUrl;
    
    private static final int QR_SIZE = 300;

    @Override
    public QrResponseDTO generarQrPersonal(QrGenerarDTO qrGenerarDTO) {
        // Validar que el tipo sea PERSONAL
        if (qrGenerarDTO.getTipo() != TipoQr.PERSONAL) {
            throw new BusinessException("El método generarQrPersonal solo acepta tipo PERSONAL");
        }
        
        // Verificar si ya existe un QR para este carnet
        String codigoEsperado = generarCodigoQr(qrGenerarDTO.getCarnetIdentidad());
        if (qrRepository.existsByCodigo(codigoEsperado)) {
            throw new BusinessException("Ya existe un QR generado para el carnet: " + qrGenerarDTO.getCarnetIdentidad());
        }
        
        return generarQr(qrGenerarDTO);
    }

    @Override
    public QrResponseDTO generarQrExterno(QrGenerarDTO qrGenerarDTO) {
        // Validar que el tipo sea EXTERNO
        if (qrGenerarDTO.getTipo() != TipoQr.EXTERNO) {
            throw new BusinessException("El método generarQrExterno solo acepta tipo EXTERNO");
        }
        
        return generarQr(qrGenerarDTO);
    }
    
    private QrResponseDTO generarQr(QrGenerarDTO qrGenerarDTO) {
        try {
            // Generar código único para el QR
            String codigo = generarCodigoQr(qrGenerarDTO.getCarnetIdentidad());
            
            // Generar la imagen QR
            String rutaImagen = generarImagenQr(codigo, qrGenerarDTO.getCarnetIdentidad());
            
            // Crear entidad QR
            Qr qr = Qr.builder()
                    .codigo(codigo)
                    .tipo(qrGenerarDTO.getTipo())
                    .estado(EstadoQr.LIBRE)
                    .rutaImagenQr(rutaImagen)
                    .build();
            
            qr = qrRepository.save(qr);
            log.info("QR generado exitosamente: {} para carnet: {}", codigo, qrGenerarDTO.getCarnetIdentidad());
            
            return mapToDTO(qr);
            
        } catch (Exception e) {
            log.error("Error al generar QR: {}", e.getMessage());
            throw new BusinessException("Error al generar el código QR: " + e.getMessage());
        }
    }
    
    private String generarCodigoQr(String carnetIdentidad) {
        // Formato: QR-CARNET-YYYYMMDD-UUID único
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("QR-%s-%s-%s", carnetIdentidad, fecha, uniqueId);
    }
    
    private String generarImagenQr(String codigo, String carnetIdentidad) throws Exception {
        // Crear directorio si no existe
        Path storagePath = Paths.get(storageProperties.getPath()).toAbsolutePath().normalize();
        if (!Files.exists(storagePath)) {
            try {
                Files.createDirectories(storagePath);
            } catch (Exception e) {
                throw new BusinessException("No se pudo crear el directorio para almacenar QR: " + e.getMessage());
            }
        }
        
        // Generar contenido del QR (puede ser una URL o información relevante)
        String qrContent = String.format("{\"tipo\":\"PERSONAL\",\"carnet\":\"%s\",\"codigo\":\"%s\"}", 
                carnetIdentidad, codigo);
        
        // Generar imagen QR
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
        
        // Nombre del archivo
        String fileName = String.format("qr-%s-%d.png", carnetIdentidad, System.currentTimeMillis());
        Path filePath = storagePath.resolve(fileName);
        
        // Guardar imagen
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);
        
        // Retornar ruta relativa
        return storageProperties.getPath() + fileName;
    }

    @Override
    public QrResponseDTO obtenerQrPorId(Long id) {
        Qr qr = qrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR no encontrado con ID: " + id));
        return mapToDTO(qr);
    }

    @Override
    public QrResponseDTO obtenerQrPorCodigo(String codigo) {
        Qr qr = qrRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("QR no encontrado con código: " + codigo));
        return mapToDTO(qr);
    }

    @Override
    public QrResponseDTO obtenerQrPorPersonalId(Long personalId) {
        Qr qr = qrRepository.findByPersonalId(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró QR para el personal ID: " + personalId));
        return mapToDTO(qr);
    }

    @Override
    public List<QrResponseDTO> listarQrsLibres() {
        return qrRepository.findByEstado(EstadoQr.LIBRE)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<QrResponseDTO> listarQrsPorTipo(TipoQr tipo) {
        return qrRepository.findByTipoAndEstado(tipo, EstadoQr.LIBRE)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Qr asignarQrAPersonal(Long qrId, Long personalId) {
        Qr qr = qrRepository.findById(qrId)
                .orElseThrow(() -> new ResourceNotFoundException("QR no encontrado con ID: " + qrId));
        
        Personal personal = personalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal no encontrado con ID: " + personalId));
        
        // Validar que el QR esté libre
        if (qr.getEstado() != EstadoQr.LIBRE) {
            throw new BusinessException("El QR no está disponible para asignación. Estado actual: " + qr.getEstado());
        }
        
        // Validar que el personal no tenga ya un QR asignado
        if (qrRepository.findByPersonalId(personalId).isPresent()) {
            throw new BusinessException("El personal ya tiene un QR asignado");
        }
        
        // Asignar QR al personal
        qr.setPersonal(personal);
        qr.setEstado(EstadoQr.ASIGNADO);
        
        qr = qrRepository.save(qr);
        log.info("QR {} asignado al personal {}", qrId, personalId);
        
        return qr;
    }

    @Override
    public Qr liberarQr(Long qrId) {
        Qr qr = qrRepository.findById(qrId)
                .orElseThrow(() -> new ResourceNotFoundException("QR no encontrado con ID: " + qrId));
        
        qr.setPersonal(null);
        qr.setEstado(EstadoQr.LIBRE);
        
        qr = qrRepository.save(qr);
        log.info("QR {} liberado", qrId);
        
        return qr;
    }

    @Override
    public Qr inactivarQr(Long qrId) {
        Qr qr = qrRepository.findById(qrId)
                .orElseThrow(() -> new ResourceNotFoundException("QR no encontrado con ID: " + qrId));
        
        qr.setEstado(EstadoQr.INACTIVO);
        qr.setPersonal(null);
        
        qr = qrRepository.save(qr);
        log.info("QR {} inactivado", qrId);
        
        return qr;
    }

    @Override
    public byte[] descargarImagenQr(Long qrId) {
        Qr qr = qrRepository.findById(qrId)
                .orElseThrow(() -> new ResourceNotFoundException("QR no encontrado con ID: " + qrId));
        
        try {
            Path imagePath = Paths.get(qr.getRutaImagenQr());
            return Files.readAllBytes(imagePath);
        } catch (IOException e) {
            log.error("Error al leer la imagen QR: {}", e.getMessage());
            throw new BusinessException("Error al leer la imagen QR");
        }
    }

    @Override
    public String getUrlPublicaQr(String rutaImagen) {
        String fileName = Paths.get(rutaImagen).getFileName().toString();
        return baseUrl + storageProperties.getUrlPrefix() + fileName;
    }
    
    private QrResponseDTO mapToDTO(Qr qr) {
        return QrResponseDTO.builder()
                .id(qr.getId())
                .codigo(qr.getCodigo())
                .tipo(qr.getTipo())
                .estado(qr.getEstado())
                .rutaImagenQr(qr.getRutaImagenQr())
                .urlPublica(getUrlPublicaQr(qr.getRutaImagenQr()))
                .createdAt(qr.getCreatedAt())
                .build();
    }
}