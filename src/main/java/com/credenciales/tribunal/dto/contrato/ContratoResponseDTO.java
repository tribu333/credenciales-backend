package com.credenciales.tribunal.dto.contrato;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoResponseDTO {
    private Long id;
    private PersonalBasicoDTO personal;
    private Boolean activo;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private CargoBasicoDTO cargo;
    private ProcesoBasicoDTO proceso;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalBasicoDTO {
        private Long id;
        private String nombre;
        private String apellido;
        private String documentoIdentidad;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CargoBasicoDTO {
        private Long id;
        private String nombre;
        private String descripcion;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcesoBasicoDTO {
        private Long id;
        private String nombre;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaFin;
    }
}