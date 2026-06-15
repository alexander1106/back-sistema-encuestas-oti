package com.gaslac.sistema_encuestas.modules.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SentimientoDTO {
    private String categoria;
    private Double porcentaje;
}
