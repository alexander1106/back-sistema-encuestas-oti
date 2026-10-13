package com.gaslac.sistema_encuestas.modules.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportacionEncuestaDTO {

    private Integer idEncuesta;
    private String nombreEncuesta;

    private List<PreguntaExportacionDTO> preguntas;

    private List<FilaExportacionDTO> filas;
}
