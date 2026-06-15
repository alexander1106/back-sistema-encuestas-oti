package com.gaslac.sistema_encuestas.modules.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PuntajeItemDTO {

    private Integer idItem;
    private Integer numeroItem;
    private String textoItem;
    private Double promedio;
    private Integer respuestasContadas;
    private Integer puntajeTotal;
}
