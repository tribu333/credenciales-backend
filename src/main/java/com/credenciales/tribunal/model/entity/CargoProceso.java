package com.credenciales.tribunal.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cargo_proceso")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CargoProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private ProcesoElectoral proceso;

    @Column(nullable = false, length = 150)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
    @Column(name = "activo") // o sin @Column si el nombre coincide
    private Boolean activo;   // o boolean
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "cargoProceso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialCargoProceso> historiales;
}
