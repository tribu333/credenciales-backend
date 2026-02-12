package com.credenciales.tribunal.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "imagen")
@Getter // Genera todos los getters
@Setter // Genera todos los setters
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
@ToString // Genera el método toString
@Builder // Permite usar el patrón Builder
public class Imagen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImagen;

    @Column(name = "nombre_archivo", nullable = false, unique = true)
    private String nombreArchivo; // UUID generado

    @Column(name = "nombre_original", nullable = false)
    private String nombreOriginal; // Nombre original del archivo subido

    @Column(name = "ruta_completa", nullable = false)
    private String rutaCompleta;

    @Column(name = "mime_type", nullable = false)
    private String mimeType; // image/png, image/jpeg, etc.

    @Column(name = "tamanio_bytes")
    private Long tamanioBytes;

    @Column(name = "tipo_imagen", length = 100)
    private String tipoImagen;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /* // Relación muchos a uno: muchas imágenes pertenecen a un complaint
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_denuncia", nullable = false)
    @ToString.Exclude // Excluye de toString para evitar recursión
    private Complaint complaint; */

    // ========== MÉTODOS ÚTILES ==========
    
    public String getTamanioFormateado() {
        if (tamanioBytes == null) return "0 KB";
        
        double kb = tamanioBytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.2f KB", kb);
        }
        double mb = kb / 1024.0;
        return String.format("%.2f MB", mb);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getFechaSubida() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFechaSubida'");
    }
}