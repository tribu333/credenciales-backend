package com.credenciales.tribunal.service;

public interface EmailService {
    void enviarCodigoVerificacion(String destinatario, String codigo, String carnetIdentidad);
}
