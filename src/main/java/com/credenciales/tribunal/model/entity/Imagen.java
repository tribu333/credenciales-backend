package com.credenciales.tribunal.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "imagen")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String nombre;

    @Column(name = "nombre_original", length = 150)
    private String nombreOriginal;

    @Column(name = "ruta_completa", length = 255)
    private String rutaCompleta;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "tamanio_bytes")
    private Long tamanioBytes;

    @Column(name = "tipo_imagen", length = 100)
    private String tipoImagen;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
