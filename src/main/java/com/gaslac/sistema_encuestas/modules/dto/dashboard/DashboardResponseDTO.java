package com.gaslac.sistema_encuestas.modules.dto.dashboard;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponseDTO {
    private KpiDashboardDTO kpis;
    private List<ComparativoEscuelaDTO> comparativoPorEscuela;
    private List<SentimientoDTO> distribucionSentimiento;
    private List<RankingPreguntaDTO> rankingCalidadPreguntas;
}
