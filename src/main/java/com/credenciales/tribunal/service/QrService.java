package com.credenciales.tribunal.service;

import com.credenciales.tribunal.dto.qr.QrGenerarDTO;
import com.credenciales.tribunal.dto.qr.QrResponseDTO;
import com.credenciales.tribunal.model.entity.Qr;
import com.credenciales.tribunal.model.enums.TipoQr;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QrService {
    
    QrResponseDTO generarQrPersonal(QrGenerarDTO qrGenerarDTO);
    
    QrResponseDTO generarQrExterno(QrGenerarDTO qrGenerarDTO);
    
    QrResponseDTO obtenerQrPorId(Long id);
    
    QrResponseDTO obtenerQrPorCodigo(String codigo);
    
    QrResponseDTO obtenerQrPorPersonalId(Long personalId);
    
    List<QrResponseDTO> listarQrsLibres();
    
    List<QrResponseDTO> listarQrsPorTipo(TipoQr tipo);
    
    Qr asignarQrAPersonal(Long qrId, Long personalId);
    
    Qr liberarQr(Long qrId);
    
    Qr inactivarQr(Long qrId);
    
    byte[] descargarImagenQr(Long qrId);
    
    String getUrlPublicaQr(String rutaImagen);
}