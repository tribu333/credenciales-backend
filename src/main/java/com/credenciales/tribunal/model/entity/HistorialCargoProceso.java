package com.credenciales.tribunal.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_cargo_proceso")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialCargoProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_proceso_id", nullable = false)
    private CargoProceso cargoProceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @Column(name = "fecha_inicio", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Boolean activo = true;
}