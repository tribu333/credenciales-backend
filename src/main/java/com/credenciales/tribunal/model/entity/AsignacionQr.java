package com.credenciales.tribunal.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asignacion_qr")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsignacionQr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "externo_id")
    private Externo externo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qr_id", nullable = false)
    private Qr qr;

    @Column(name = "fecha_asignacion", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_liberacion")
    private LocalDateTime fechaLiberacion;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    @Builder.Default
    private Boolean activo = true;
}
