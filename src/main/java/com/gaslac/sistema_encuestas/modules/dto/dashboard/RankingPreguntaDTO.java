package com.gaslac.sistema_encuestas.modules.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingPreguntaDTO {
    private String codigo;
    private Integer numero;
    private String descripcion;
    private Double promedio;
    private String estado;
}
