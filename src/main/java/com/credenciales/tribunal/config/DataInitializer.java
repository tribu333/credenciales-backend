package com.credenciales.tribunal.config;

import com.credenciales.tribunal.model.entity.Estado;
import com.credenciales.tribunal.model.enums.EstadoPersonal;
import com.credenciales.tribunal.repository.EstadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final EstadoRepository estadoRepository;

    @Override
    public void run(String... args) throws Exception {
        // Crear estados si no existen
        for (EstadoPersonal estadoEnum : EstadoPersonal.values()) {
            if (estadoRepository.findByNombre(estadoEnum.getNombre()).isEmpty()) {
                Estado estado = Estado.builder()
                        .nombre(estadoEnum.getNombre())
                        .build();
                estadoRepository.save(estado);
                log.info("Estado creado: {}", estadoEnum.getNombre());
            }
        }
    }
}