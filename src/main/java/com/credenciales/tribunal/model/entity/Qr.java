package com.credenciales.tribunal.model.entity;

import com.credenciales.tribunal.model.enums.EstadoQr;
import com.credenciales.tribunal.model.enums.TipoQr;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "qr")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Qr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoQr tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "ENUM('LIBRE','ASIGNADO','INACTIVO') DEFAULT 'LIBRE'")
    private EstadoQr estado = EstadoQr.LIBRE;

    @Column(name = "ruta_imagen_qr", nullable = false, length = 255)
    private String rutaImagenQr;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relaciones
    @OneToOne(mappedBy = "qr")
    private Personal personal;

    @OneToMany(mappedBy = "qr")
    private List<AsignacionQr> asignaciones;

    @OneToMany(mappedBy = "qr")
    private List<Acceso> accesos;
}
