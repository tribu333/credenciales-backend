package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Override
    public void enviarCodigoVerificacion(String destinatario, String codigo, String carnetIdentidad) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(destinatario);
            message.setSubject("Código de verificación - Registro Personal");
            message.setText(String.format(
                    "TRIBUNAL ELECTORAL DEPARTAMENTAL DE COCHABAMBA \n\n" +
                            "Tu código de verificación es: %s\n\n" +
                            "Este código expirará en 10 minutos.\n\n" +
                            "Si no solicitaste este código, por favor ignora este mensaje.\n\n" +
                            "Saludos,\n", codigo
            ));
            
            mailSender.send(message);
            log.info("Código de verificación enviado a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email: {}", e.getMessage());
            throw new RuntimeException("Error al enviar el código de verificación");
        }
    }
}
