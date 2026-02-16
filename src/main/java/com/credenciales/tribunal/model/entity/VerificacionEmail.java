package com.credenciales.tribunal.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verificacion_email")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerificacionEmail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 150)
    private String email;
    
    @Column(nullable = false, length = 6)
    private String codigo;
    
    @Column(nullable = false, length = 20)
    private String carnetIdentidad;
    
    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean utilizado = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}