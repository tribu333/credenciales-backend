package com.credenciales.tribunal.model.entity;

import com.credenciales.tribunal.model.enums.TipoExterno;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "externo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Externo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", length = 150)
    private String nombreCompleto;

    @Column(name = "carnet_identidad", length = 50)
    private String carnetIdentidad;

    @Column(length = 100)
    private String identificador;

    @Column(name = "org_politica", length = 150)
    private String orgPolitica;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_externo_id", nullable = false, length = 30)
    private TipoExterno tipoExterno;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;

    @OneToMany(mappedBy = "externo")
    private List<AsignacionQr> asignaciones;
}
