package com.credenciales.tribunal.dto.image;
import java.time.LocalDateTime;
import lombok.*;
@Getter // Genera todos los getters
@Setter // Genera todos los setters
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
@ToString // Genera el método toString
@Builder // Permite usar el patrón Builder
public class ImagenResponseDTO {
    
    private Long idImagen;
    private String nombreArchivo;
    private String nombreOriginal;
    private String urlDescarga; // URL para descargar la imagen
    private String mimeType;
    private Long tamanioBytes;
    private String tamanioFormateado;
    private LocalDateTime fechaSubida;
    private Long idComplaint;
}