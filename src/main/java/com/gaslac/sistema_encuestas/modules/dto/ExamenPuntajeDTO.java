package com.gaslac.sistema_encuestas.modules.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamenPuntajeDTO {

    private Integer idEncuesta;
    private String nombre;
    private Integer puntajeTotal;
    private Double promedioGeneral;
    private Integer respuestasGuardadas;
    private List<PuntajeItemDTO> detalleItems;
}
