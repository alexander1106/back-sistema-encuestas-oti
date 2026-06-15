package com.gaslac.sistema_encuestas.modules.dto;

import lombok.Data;

@Data

public class RespuestaDTO {

    private Integer idUsuario;
    private Integer idItem;
    private String valor;

}