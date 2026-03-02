package com.credenciales.tribunal.dto.personal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@Schema(description = "DTO para certificados de personal")
public class PersonalCertificadoDTO {
    private Long id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String carnetIdentidad;
    private String estadoActual;
    private String cargo;
    private String descripcion;
    private String proceso;
    private String fecha_ini;
    private String fecha_fin;

    public PersonalCertificadoDTO(Long id, String nombre, String apellidoPaterno,
            String apellidoMaterno, String carnetIdentidad,
            String estadoActual, String cargo,
            String descripcion, String fecha_ini,
            String fecha_fin, String proceso) {
        this.id = id;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.carnetIdentidad = carnetIdentidad;
        this.estadoActual = estadoActual;
        this.cargo = cargo;
        this.descripcion = descripcion;
        this.fecha_ini = fecha_ini;
        this.fecha_fin = fecha_fin;
        this.proceso = proceso;
    }
}