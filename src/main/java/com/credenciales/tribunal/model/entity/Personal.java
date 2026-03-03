package com.credenciales.tribunal.model.entity;

import com.credenciales.tribunal.model.enums.TipoPersonal;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "personal")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Personal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s.]+$", 
            message = "El nombre solo puede contener letras, espacios y puntos")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s.]*$", 
            message = "El apellido paterno solo puede contener letras, espacios y puntos")
    @Column(name = "apellido_paterno", length = 100)
    private String apellidoPaterno;

    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s.]*$", 
            message = "El apellido materno solo puede contener letras, espacios y puntos")
    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;

    @NotBlank(message = "El carnet de identidad es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,15}$", 
            message = "El carnet de identidad debe tener entre 4 y 15 caracteres alfanuméricos sin espacios")
    @Column(name = "carnet_identidad", nullable = false, length = 50)
    private String carnetIdentidad;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Column(nullable = false, length = 150)
    private String correo;

    @Pattern(regexp = "^(\\+?\\d{1,3}[-.]?)?\\(?\\d{1,4}\\)?[-.]?\\d{1,4}[-.]?\\d{1,9}$|^$", 
            message = "El celular debe contener solo números y puede incluir +, (), -")
    @Column(length = 30)
    private String celular;

    @NotNull(message = "El acceso a cómputo es obligatorio")
    @Column(name = "acceso_computo", nullable = false, columnDefinition = "TINYINT default 0")
    @ColumnDefault("0")
    @Builder.Default
    private Boolean accesoComputo = false;  // Valor por defecto en Java

    @Column(name = "nro_circunscripcion", length = 10)
    private String nroCircunscripcion;

    @Column(name = "token_personal", length = 255)
    private String tokenPersonal;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private TipoPersonal tipo = TipoPersonal.EVENTUAL;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relaciones
    @NotNull(message = "La imagen es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_id", nullable = false)
    private Imagen imagen;

    @NotNull(message = "El QR es obligatorio")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qr_id", nullable = false)
    private Qr qr;

    @OneToMany(mappedBy = "personal", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<HistorialCargo> historialCargos;

    @OneToMany(mappedBy = "personal", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<HistorialCargoProceso> historialCargosProceso;

    @OneToMany(mappedBy = "personal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EstadoActual> estadosActuales;
}
