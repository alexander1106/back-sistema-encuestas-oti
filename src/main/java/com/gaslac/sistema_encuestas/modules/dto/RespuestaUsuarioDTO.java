package com.gaslac.sistema_encuestas.modules.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespuestaUsuarioDTO {

    private Integer idRespuesta;
    private Integer idItem;
    private Integer numeroItem;
    private String textoItem;
    private String valor;
    private LocalDateTime fecha;
}
