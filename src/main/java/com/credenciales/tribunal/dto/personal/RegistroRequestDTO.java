package com.credenciales.tribunal.dto.personal;

import lombok.Data;

@Data
public class RegistroRequestDTO {

    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String carnetIdentidad;
    private String correo;
    private String celular;
}
