package com.gaslac.sistema_encuestas.modules.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteCompletoDTO {

    private ExamenPuntajeDTO examen;
    private List<PromedioCarreraDTO> carreras;
    private KpiExamenDTO kpi;
}