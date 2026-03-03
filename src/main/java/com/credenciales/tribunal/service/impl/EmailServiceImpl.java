package com.credenciales.tribunal.service.impl;

import com.credenciales.tribunal.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject("Código de verificación - Registro Personal");

            String contenidoHtml = "<div style=\"font-family: Arial, sans-serif; text-align: center; padding: 20px; border: 1px solid #eee; border-radius: 10px; max-width: 500px; margin: auto;\">" +
                    "<h2 style=\"color: #1E2B5E;\">Confirmación de Registro</h2>" +
                    "<p>Hola</b>,</p>" +
                    "<p>Estás a un paso de completar tu registro en el Sistema de Personal Eventual.</p>" +
                    "<p>Tu código de seguridad es:</p>" +
                    "<div style=\"background-color: #f9f9f9; padding: 15px; border-radius: 8px; margin: 20px 0;\">" +
                    "<h1 style=\"color: #FFD54F; letter-spacing: 10px; font-size: 38px; margin: 0;\">" + codigo + "</h1>" +
                    "</div>" +
                    "<p style=\"color: gray; font-size: 12px; margin-top: 30px;\">Si no solicitaste este código, puedes ignorar este mensaje de forma segura.</p>" +
                    "</div>";

            helper.setText(contenidoHtml, true); // true indica que es HTML

            mailSender.send(message);
            log.info("Código de verificación enviado a: {} (C.I.: {})", destinatario, carnetIdentidad);
        } catch (MessagingException e) {
            log.error("Error al enviar email: {}", e.getMessage());
            throw new RuntimeException("Error al enviar el código de verificación", e);
        }
    }
}
