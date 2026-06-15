package com.gaslac.sistema_encuestas.modules.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KpiDashboardDTO {
    private Integer totalRespuestas;
    private Integer egresadosEncuestados;
    private Double promedioSatisfaccion;
    private String mejorEscuela;
    private String peorEscuela;
    private Double tasaParticipacion;
}
