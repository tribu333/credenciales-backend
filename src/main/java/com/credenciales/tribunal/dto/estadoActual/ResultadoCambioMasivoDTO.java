package com.credenciales.tribunal.dto.estadoActual;

import com.credenciales.tribunal.dto.personal.PersonalDTO;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ResultadoCambioMasivoDTO {
    private int totalProcesados;
    private int exitosos;
    private int fallidos;
    private List<Long> idsExitosos;
    private Map<Long, String> errores;
    private List<PersonalDTO> personalesActualizados;
}