package com.credenciales.tribunal.model.entity;


import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "unidad")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 250)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String abreviatura;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    @Builder.Default
    private Boolean estado = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relaciones
    @OneToMany(mappedBy = "unidad")
    private List<Cargo> cargos;

    @OneToMany(mappedBy = "unidad")
    private List<CargoProceso> cargosProceso;
}
