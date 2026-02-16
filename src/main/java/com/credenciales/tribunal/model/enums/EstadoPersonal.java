package com.credenciales.tribunal.model.enums;

public enum EstadoPersonal {
    PERSONAL_REGISTRADO("PERSONAL REGISTRADO"),
    CREDENCIAL_IMPRESO("CREDENCIAL IMPRESO"),
    CREDENCIAL_ENTREGADO("CREDENCIAL ENTREGADO"),
    PERSONAL_ACTIVO("PERSONAL ACTIVO"),
    PERSONAL_CON_ACCESO_A_COMPUTO("PERSONAL CON ACCESO A COMPUTO"),
    CREDENCIAL_DEVUELTO("CREDENCIAL DEVUELTO"),
    PERSONAL_INACTIVO_PROCESO_TERMINADO("INACTIVO PROCESO ELECTORAL TERMINADO"),
    INACTIVO_POR_RENUNCIA("INACTIVO POR RENUNCIA");

    private final String nombre;

    EstadoPersonal(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
