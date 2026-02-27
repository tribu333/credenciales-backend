package com.credenciales.tribunal.service;

import java.util.List;

import com.credenciales.tribunal.dto.contrato.ContratoCreateRequestDTO;
import com.credenciales.tribunal.dto.contrato.ContratoResponseDTO;
import com.credenciales.tribunal.dto.contrato.ContratoUpdateRequestDTO;

public interface ContratoService {

    ContratoResponseDTO createContrato(ContratoCreateRequestDTO requestDTO);
    ContratoResponseDTO getContratoById(Long id);

    List<ContratoResponseDTO> getAllContratos();
    ContratoResponseDTO updateContrato(Long id, ContratoUpdateRequestDTO requestDTO);
}
