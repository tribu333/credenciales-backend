package com.credenciales.tribunal.dto.historialcargoproceso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCargoProcesoResumenListDTO {
    private Integer total;
    private List<HistorialCargoProcesoResumenDTO> historiales;
    
}
