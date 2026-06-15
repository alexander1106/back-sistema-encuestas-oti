package com.gaslac.sistema_encuestas.modules.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComparativoEscuelaDTO {
    private String escuelaProfesional;
    private Double promedioSatisfaccion;
    private Integer totalRespuestas;
}
