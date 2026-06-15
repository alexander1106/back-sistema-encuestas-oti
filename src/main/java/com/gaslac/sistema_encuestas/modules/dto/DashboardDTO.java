package com.gaslac.sistema_encuestas.modules.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private DashboardKpisDTO kpis;
    private List<ComparativoEscuelaDTO> comparativoPorEscuela;
    private List<SentimientoDTO> distribucionSentimiento;
    private List<RankingPreguntaDTO> rankingCalidadPreguntas;
}
