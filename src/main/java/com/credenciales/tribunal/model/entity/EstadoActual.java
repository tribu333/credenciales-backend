package com.credenciales.tribunal.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "estado_actual")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstadoActual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;
    
    @ManyToOne
    @JoinColumn(name = "estado_id", nullable = false)
    private Estado estado;
    
    @Column(name = "valor_estado_actual",nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    @Builder.Default
    private Boolean valor_estado_actual = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
