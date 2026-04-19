package com.credenciales.tribunal.dto.procesoelectoral;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoElectoralResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean estado;
    private String estadoDescripcion; // "ACTIVO", "FINALIZADO", "PROXIMO"
    private Long imagenId;
    private String imagenNombre;
    private String imagenUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalCargos;
    private Integer totalCargosActivos;
    private Long duracionDias;
    private Boolean vigente;
}
