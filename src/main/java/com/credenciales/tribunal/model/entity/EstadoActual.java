package com.credenciales.tribunal.model.entity;

import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "estado_actual")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstadoActual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación Many-to-One con Personal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    @JsonIgnoreProperties({"estadosActuales", "historialCargos", "historialCargosProceso"})
    private Personal personal;

    // Relación Many-to-One con Estado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    @JsonIgnoreProperties("estadosActuales")
    private Estado estado;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
