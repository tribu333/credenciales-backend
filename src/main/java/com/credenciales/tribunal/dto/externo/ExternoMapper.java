/* package com.credenciales.tribunal.dto.externo;

import com.credenciales.tribunal.dto.externo.ExternoRequestDTO;
import com.credenciales.tribunal.dto.externo.ExternoDetalleResponseDTO;
import com.credenciales.tribunal.dto.externo.ExternoResponseDTO;
import com.credenciales.tribunal.dto.externo.ImagenBasicaDTO;
import com.credenciales.tribunal.model.entity.Externo;
import com.credenciales.tribunal.model.entity.Imagen;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ExternoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "imagen", ignore = true)
    @Mapping(target = "asignaciones", ignore = true)
    Externo toEntity(ExternoRequestDTO dto);

    ExternoResponseDTO toResponseDTO(Externo externo);

    ExternoDetalleResponseDTO toDetalleResponseDTO(Externo externo);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre") // Ajusta según tu entidad Imagen
    @Mapping(target = "url", source = "url") // Ajusta según tu entidad Imagen
    @Mapping(target = "tipoContenido", source = "tipoContenido") // Ajusta según tu entidad Imagen
    ImagenBasicaDTO toImagenBasicaDTO(Imagen imagen);

    // Método helper para actualizar entidad existente
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "imagen", ignore = true)
    @Mapping(target = "asignaciones", ignore = true)
    void updateEntityFromDTO(ExternoRequestDTO dto, @MappingTarget Externo externo);
} */