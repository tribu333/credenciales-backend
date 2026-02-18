package com.credenciales.tribunal.dto.image;
import lombok.*;
@Getter // Genera todos los getters
@Setter // Genera todos los setters
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
@ToString // Genera el método toString
@Builder
public class ImagenBasicaDTO {
    private Long idImagen;
    private String nombreArchivo;
    private String nombreOriginal;
    private String urlDescarga;
}
