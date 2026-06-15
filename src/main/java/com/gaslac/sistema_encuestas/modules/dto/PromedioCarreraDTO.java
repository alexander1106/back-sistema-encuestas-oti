package com.gaslac.sistema_encuestas.modules.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromedioCarreraDTO {
    private Integer idCarrera;
    private String nombreCarrera;
    private Double promedio;
    private Integer cantidadRespuestas;
}