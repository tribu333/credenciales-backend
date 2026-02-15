package com.credenciales.tribunal.dto.procesoelectoral;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoElectoralSearchRequestDTO {
    
    private String nombre;
    
    private Boolean estado;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicioDesde;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicioHasta;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFinDesde;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFinHasta;
    
    private Boolean vigente; // true = vigentes, false = no vigentes
}