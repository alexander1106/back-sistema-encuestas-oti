package com.gaslac.sistema_encuestas.modules.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoExamenDTO {

    private Integer idUsuario;
    private Integer puntajeTotal;
    private Double puntajePromedio;
    private Integer respuestasGuardadas;
}
