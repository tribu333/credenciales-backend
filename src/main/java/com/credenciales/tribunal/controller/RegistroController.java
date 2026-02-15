package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.personal.RegistroRequestDTO;
import com.credenciales.tribunal.dto.personal.VerificacionCorreoDTO;
import com.credenciales.tribunal.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registro")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;

    @PostMapping("/solicitar")
    public String solicitar(@RequestBody RegistroRequestDTO request) {
        registroService.solicitarRegistro(request);
        return "Código enviado";
    }

    @PostMapping("/verificar")
    public String verificar(@RequestBody VerificacionCorreoDTO request) {
        registroService.verificarCodigo(request);
        return "Verificado correctamente";
    }
}
