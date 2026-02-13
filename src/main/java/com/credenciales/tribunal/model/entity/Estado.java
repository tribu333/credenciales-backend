package com.credenciales.tribunal.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "estado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_estado", nullable = false, length = 45)
    private String nombreEstado;

    @Column(name = "valor_estado", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Boolean valorEstado = false;

    // Relación bidireccional con EstadoActual
    @OneToMany(mappedBy = "estado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstadoActual> estadosActuales;
}