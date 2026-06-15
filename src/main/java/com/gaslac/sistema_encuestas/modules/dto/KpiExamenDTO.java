package com.gaslac.sistema_encuestas.modules.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KpiExamenDTO {
    private Double promedioGeneral;
    private Integer totalRespuestas;
    private Double puntajeMaximo;
    private Double valorEsperado;
    private long participantes;
}