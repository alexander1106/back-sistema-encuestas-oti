package com.gaslac.sistema_encuestas.modules.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreguntaExportacionDTO {

    private Integer idItem;
    private Integer numero;
    private String texto;

    private Integer idDimension;
    private String dimensionNombre;
    private String dimensionCodigo;
}
