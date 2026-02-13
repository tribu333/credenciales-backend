package com.credenciales.tribunal.model.entity;


import com.credenciales.tribunal.model.enums.TipoPersonal;
import lombok.*;
import main.java.com.credenciales.tribunal.model.entity.EstadoActual;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "personal")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Personal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido_paterno", length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;

    @Column(name = "carnet_identidad", nullable = false, length = 50, unique = true)
    private String carnetIdentidad;

    @Column(nullable = false, length = 150, unique = true)
    private String correo;

    @Column(length = 30)
    private String celular;

    @Column(name = "acceso_computo", nullable = false, columnDefinition = "TINYINT")
    private Boolean accesoComputo;

    @Column(name = "nro_circunscripcion", length = 10)
    private String nroCircunscripcion;

    @Column(name = "token_personal", length = 255)
    private String tokenPersonal;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TipoPersonal tipo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_id", nullable = false)
    private Imagen imagen;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qr_id", nullable = false)
    private Qr qr;

    @OneToMany(mappedBy = "personal", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<HistorialCargo> historialCargos;

    @OneToMany(mappedBy = "personal", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<HistorialCargoProceso> historialCargosProceso;

    @OneToMany(mappedBy = "personal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstadoActual> estadosActuales;
}
