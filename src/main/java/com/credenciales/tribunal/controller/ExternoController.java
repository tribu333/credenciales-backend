package com.credenciales.tribunal.controller;

import com.credenciales.tribunal.dto.externo.ExternoDTO;
import com.credenciales.tribunal.dto.externo.ExternoDetalleResponseDTO;
import com.credenciales.tribunal.dto.externo.ExternoRequestDTO;
import com.credenciales.tribunal.dto.externo.ExternoResponseDTO;
import com.credenciales.tribunal.model.enums.TipoExterno;
import com.credenciales.tribunal.service.ExternoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/externos")
@RequiredArgsConstructor
public class ExternoController {

    private final ExternoService externoService;

    @PostMapping
    public ResponseEntity<ExternoResponseDTO> crearExterno(@Valid @RequestBody ExternoRequestDTO requestDTO) {
        return new ResponseEntity<>(externoService.crearExterno(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExternoResponseDTO> actualizarExterno(
            @PathVariable Long id,
            @Valid @RequestBody ExternoRequestDTO requestDTO) {
        return ResponseEntity.ok(externoService.actualizarExterno(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarExterno(@PathVariable Long id) {
        externoService.eliminarExterno(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternoDTO> obtenerExterno(@PathVariable Long id) {
        return ResponseEntity.ok(externoService.obtenerExternoPorId(id));
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<ExternoDetalleResponseDTO> obtenerDetalleExterno(@PathVariable Long id) {
        return ResponseEntity.ok(externoService.obtenerExternoDetallePorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ExternoDTO>> listarTodos() {
        return ResponseEntity.ok(externoService.listarTodos());
    }

    @GetMapping("/response")
    public ResponseEntity<List<ExternoResponseDTO>> listarTodosResponse() {
        return ResponseEntity.ok(externoService.listarTodosResponse());
    }

    @GetMapping("/detalles")
    public ResponseEntity<List<ExternoDetalleResponseDTO>> listarTodosDetalles() {
        return ResponseEntity.ok(externoService.listarTodosDetalles());
    }

    @GetMapping("/carnet/{carnetIdentidad}")
    public ResponseEntity<ExternoDTO> obtenerPorCarnet(@PathVariable String carnetIdentidad) {
        return ResponseEntity.ok(externoService.obtenerPorCarnetIdentidad(carnetIdentidad));
    }

    // Actualizado: ahora devuelve una lista
    @GetMapping("/identificador/{identificador}")
    public ResponseEntity<List<ExternoDTO>> listarPorIdentificador(@PathVariable String identificador) {
        return ResponseEntity.ok(externoService.listarPorIdentificador(identificador));
    }

    // Nuevo endpoint: búsqueda parcial por identificador
    @GetMapping("/identificador/buscar")
    public ResponseEntity<List<ExternoDTO>> listarPorIdentificadorParcial(
            @RequestParam String texto) {
        return ResponseEntity.ok(externoService.listarPorIdentificadorParcial(texto));
    }

    @GetMapping("/tipo/{tipoExterno}")
    public ResponseEntity<List<ExternoDTO>> listarPorTipo(@PathVariable TipoExterno tipoExterno) {
        return ResponseEntity.ok(externoService.listarPorTipoExterno(tipoExterno));
    }

    @GetMapping("/organizacion")
    public ResponseEntity<List<ExternoDTO>> listarPorOrganizacion(
            @RequestParam String nombre) {
        return ResponseEntity.ok(externoService.listarPorOrganizacionPolitica(nombre));
    }

    @GetMapping("/existe/carnet")
    public ResponseEntity<Boolean> existePorCarnet(@RequestParam String carnetIdentidad) {
        return ResponseEntity.ok(externoService.existePorCarnetIdentidad(carnetIdentidad));
    }

    @GetMapping("/existe/identificador")
    public ResponseEntity<Boolean> existeAlgunoPorIdentificador(@RequestParam String identificador) {
        return ResponseEntity.ok(externoService.existeAlgunoPorIdentificador(identificador));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> contarTotal() {
        return ResponseEntity.ok(externoService.contarTotal());
    }

    @GetMapping("/count/tipo/{tipoExterno}")
    public ResponseEntity<Long> contarPorTipo(@PathVariable TipoExterno tipoExterno) {
        return ResponseEntity.ok(externoService.contarPorTipoExterno(tipoExterno));
    }

    @GetMapping("/count/identificador/{identificador}")
    public ResponseEntity<Long> contarPorIdentificador(@PathVariable String identificador) {
        return ResponseEntity.ok(externoService.contarPorIdentificador(identificador));
    }
}